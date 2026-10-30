# Cloudflare Workers 部署

[GitHub Actions 工作流](../.github/workflows/deploy-worker.yml)在每次分支 Push 时构建 Web 版 Kotlin/Wasm。`xiaoqing-island` 分支执行 `wrangler deploy`，更新现有的 `muse-island.wlhx.workers.dev`；其他分支执行 `wrangler versions upload`，生成版本预览地址，不更新正式站点。静态资源目录由根目录的 [`wrangler.jsonc`](../wrangler.jsonc) 指定。

首次启用前，在 Cloudflare 控制台的 Workers & Pages → Account Details 中复制 **Account ID**（不是登录邮箱），并创建限定到该账号的 **Edit Cloudflare Workers** API Token。然后在 GitHub 仓库 `Ezioer/GameLand` 的 Settings → Secrets and variables → Actions 中添加两个 Repository secrets：

| 名称 | 内容 |
| --- | --- |
| `CLOUDFLARE_ACCOUNT_ID` | Cloudflare Account ID |
| `CLOUDFLARE_API_TOKEN` | Cloudflare API Token |

不要将 API Token 或密钥写入仓库。若该 Worker 已连接 Cloudflare Builds 的 Git 自动部署，先关闭其自动部署，以免同一次 Push 发生重复发布。完成配置后，Push 任意分支即可触发工作流。
