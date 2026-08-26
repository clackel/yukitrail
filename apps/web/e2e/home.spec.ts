import { expect, test } from '@playwright/test'

test('shows the YukiTrail project entry page', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByRole('heading', { name: '把旅程变成清晰的每一天' })).toBeVisible()
  await expect(page.getByText('规划旅程，留下足迹。')).toBeVisible()
})

