# Local intelligence substrate

Run these commands from the repository root with Docker Compose available.
This CPU-only quick-start runs LiteLLM and Ollama with one local model,
`qwen2.5:0.5b` (approximately 398 MB). Initial startup requires downloading
the container images and model.

## Start

```sh
docker compose -f infra/local/compose.yaml up -d --wait
docker compose -f infra/local/compose.yaml exec -T ollama ollama pull qwen2.5:0.5b
```

Ollama stores its model data in the Git-ignored `infra/local/.ollama/` directory.
Both HTTP ports bind only to localhost. No API keys are needed.

## Check

```sh
docker compose -f infra/local/compose.yaml ps
curl --fail-with-body -sS http://localhost:11434/api/tags
curl --fail-with-body -sS http://localhost:4000/health/readiness
```

Test Ollama directly:

```sh
curl --fail-with-body -sS http://localhost:11434/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"model":"qwen2.5:0.5b","messages":[{"role":"user","content":"Reply with exactly: ARC_OK"}],"stream":false,"options":{"temperature":0,"seed":42,"num_predict":16}}'
```

Test the same model through LiteLLM:

```sh
curl --fail-with-body -sS --max-time 120 \
  -w '\nHTTP %{http_code}\n' \
  http://localhost:4000/v1/chat/completions \
  -H 'Content-Type: application/json' \
  -d '{"model":"qwen2.5:0.5b","messages":[{"role":"user","content":"Reply with exactly: ARC_OK"}],"temperature":0,"seed":42,"max_tokens":16,"stream":false}'
```

Expected: HTTP 200 and assistant content `ARC_OK`.

## Stop

```sh
docker compose -f infra/local/compose.yaml down
```

Model data is retained for the next start.

## References

- [Official LiteLLM container deployment](https://docs.litellm.ai/docs/proxy/deploy)
- [LiteLLM Ollama provider configuration](https://docs.litellm.ai/docs/providers/ollama)
- [Official Ollama Docker instructions](https://docs.ollama.com/docker)
- [Ollama Qwen2.5 0.5B model](https://ollama.com/library/qwen2.5:0.5b)
