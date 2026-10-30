# Cloudflare Pages 部署

[GitHub Actions 工作流](../.github/workflows/deploy-pages.yml)在每次分支 Push 时构建 Kotlin/Wasm，并将 `webApp/build/dist/wasmJs/productionExecutable` 上传到现有的 `muse-island` Pages 项目。`xiaoqing-island` 是正式分支，更新 <https://muse-island.pages.dev/>；其他分支生成 Pages 预览部署。旧 Worker 不再由 GitHub Actions 部署。

GitHub 仓库 `Ezioer/GameLand` 的 Settings → Secrets and variables → Actions 需要以下 Repository secrets：

| 名称 | 内容 |
| --- | --- |
| `CLOUDFLARE_ACCOUNT_ID` | Cloudflare Account ID |
| `CLOUDFLARE_API_TOKEN` | 具有该账号 **Cloudflare Pages: Edit** 权限的 API Token |

不要将 Secret 值写入仓库。正式分支运行时，工作流会通过 Cloudflare API 检查并设置 Pages 项目的生产分支为 `xiaoqing-island`，再上传产物并验证部署环境。如果该 Pages 项目还启用了 Cloudflare 自带的 Git 自动构建，应关闭重复的自动部署。
