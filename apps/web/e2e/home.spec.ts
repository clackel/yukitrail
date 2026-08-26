import { expect, test } from '@playwright/test'

const user = {
  id: 7,
  email: 'traveler@example.com',
  nickname: '雪路',
}

const success = (data: unknown) => ({
  code: 'OK',
  message: '请求成功',
  data,
  traceId: 'e2e-trace-id',
})

test('registers, restores the session after reload, and logs out', async ({ page }) => {
  let sessionActive = false

  await page.route('**/api/v1/auth/refresh', async (route) => {
    if (!sessionActive) {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 'INVALID_REFRESH_SESSION',
          message: '刷新会话无效或已过期，请重新登录',
          data: null,
          traceId: 'e2e-trace-id',
        }),
      })
      return
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(success({
        accessToken: 'refreshed-access-token',
        tokenType: 'Bearer',
        expiresIn: 900,
        user,
      })),
    })
  })

  await page.route('**/api/v1/auth/register', async (route) => {
    sessionActive = true
    await route.fulfill({
      status: 201,
      contentType: 'application/json',
      body: JSON.stringify(success({
        accessToken: 'registered-access-token',
        tokenType: 'Bearer',
        expiresIn: 900,
        user,
      })),
    })
  })

  await page.route('**/api/v1/auth/logout', async (route) => {
    sessionActive = false
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(success(null)),
    })
  })

  await page.route('**/api/v1/health', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(success({ service: 'yukitrail-api', status: 'UP' })),
    })
  })

  await page.goto('/')
  await expect(page).toHaveURL(/\/login\?redirect=/)
  await page.getByRole('link', { name: '创建账户' }).click()

  await page.getByLabel('昵称').fill('雪路')
  await page.getByLabel('邮箱').fill('traveler@example.com')
  await page.getByLabel('密码').fill('correct-horse')
  await page.getByRole('button', { name: '创建并登录' }).click()

  await expect(page).toHaveURL(/\/$/)
  await expect(page.getByRole('heading', { name: /把旅程变成\s+清晰的每一天/ })).toBeVisible()
  await expect(page.getByText('traveler@example.com')).toBeVisible()

  await page.reload()
  await expect(page.getByText('traveler@example.com')).toBeVisible()

  await page.getByRole('button', { name: '退出' }).click()
  await expect(page).toHaveURL(/\/login$/)
})
