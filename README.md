# Controle de Gastos

App simples para registrar gastos pelo celular. Você digita o valor, e o sistema salva a data e a hora sozinho.

**Tecnologias:** Java 21 + Spring Boot 3 · HTML/CSS/JS puro · MySQL (TiDB Cloud)

## Estrutura

```
src/main/java/com/silvano/gastos/
├── model/Gasto.java            → a tabela "gastos"
├── repository/GastoRepository  → consultas ao banco
├── service/GastoService        → regras (data/hora automática, filtros)
├── controller/GastoController  → API REST (/api/gastos)
└── config/PinFilter            → protege a API com um PIN
src/main/resources/static/
├── index.html                  → tela de lançar gasto
├── historico.html              → tela de histórico + filtros
├── css/style.css · js/api.js
```

## API

| Método | Rota | O que faz |
|---|---|---|
| POST | `/api/gastos` | `{ "valor": 12.50, "descricao": "Almoço" }` (a descrição é opcional) |
| GET | `/api/gastos?periodo=MES` | `MES`, `7D`, `15D`, `30D`, `3M`, `6M`, `12M` |
| DELETE | `/api/gastos/{id}` | exclui um gasto |

---

## 1. Criar o banco no TiDB Cloud (grátis)

1. Crie uma conta em https://tidbcloud.com e crie um cluster **Starter/Serverless**.
2. Clique em **Connect**, gere uma senha e anote: **Host**, **Port (4000)**, **User** e **Password**.
3. No **SQL Editor** do TiDB, rode:
   ```sql
   CREATE DATABASE gastos;
   ```
   A tabela é criada automaticamente quando o app sobe.

## 2. Subir o código no GitHub

```bash
git init
git add .
git commit -m "Controle de gastos"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/controle-gastos.git
git push -u origin main
```

## 3. Publicar no Render (grátis)

> **Por que Render e não Vercel?** A Vercel não roda aplicações Java/Spring Boot. O Render roda, usando o `Dockerfile` do projeto.

1. Em https://render.com → **New +** → **Web Service** → conecte o repositório.
2. **Language/Runtime:** `Docker` · **Instance Type:** `Free`.
3. Em **Environment Variables**, adicione:

| Variável | Exemplo |
|---|---|
| `DB_HOST` | `gateway01.us-east-1.prod.aws.tidbcloud.com` |
| `DB_PORT` | `4000` |
| `DB_NAME` | `gastos` |
| `DB_USER` | `xxxxxxxx.root` |
| `DB_PASSWORD` | sua senha do TiDB |
| `APP_PIN` | um PIN seu, ex: `4821` |

4. Clique em **Deploy**. Quando terminar, abra a URL `https://seu-app.onrender.com`.

## 4. Deixar como "app" no celular

- **Android (Chrome):** menu ⋮ → *Adicionar à tela inicial*
- **iPhone (Safari):** Compartilhar → *Adicionar à Tela de Início*

No primeiro acesso ele pede o PIN, e depois lembra neste aparelho.

---

## Rodar no seu computador

Não precisa ter o Maven instalado: o projeto já vem com o **Maven Wrapper** (`mvnw`/`mvnw.cmd`), que baixa o Maven sozinho na primeira execução. Só precisa do **Java 21** (`winget install EclipseAdoptium.Temurin.21.JDK`).

### Com MySQL local (em vez do TiDB)

1. Instale o MySQL (ex: `winget install Oracle.MySQL`, ou o MySQL Installer) e deixe o serviço rodando.
2. Crie o banco e um usuário dedicado para o app:
   ```sql
   CREATE DATABASE gastos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'gastos_app'@'localhost' IDENTIFIED BY 'uma-senha-forte';
   GRANT ALL PRIVILEGES ON gastos.* TO 'gastos_app'@'localhost';
   FLUSH PRIVILEGES;
   ```
   A tabela `gastos` é criada sozinha quando o app sobe (`spring.jpa.hibernate.ddl-auto=update`).
3. Rode o app apontando para o MySQL local (porta padrão `3306`, sem TLS):

   **PowerShell:**
   ```powershell
   $env:DB_HOST="localhost"; $env:DB_PORT="3306"; $env:DB_NAME="gastos"
   $env:DB_USER="gastos_app"; $env:DB_PASSWORD="uma-senha-forte"; $env:DB_SSL_MODE="PREFERRED"
   $env:APP_PIN="1234"
   .\mvnw.cmd spring-boot:run
   ```
   **Linux/Mac:**
   ```bash
   export DB_HOST=localhost DB_PORT=3306 DB_NAME=gastos DB_USER=gastos_app DB_PASSWORD=uma-senha-forte DB_SSL_MODE=PREFERRED APP_PIN=1234
   ./mvnw spring-boot:run
   ```
4. Abra http://localhost:8080

### Com TiDB Cloud (igual à produção)

Mesma coisa, mas sem definir `DB_PORT` nem `DB_SSL_MODE` (os padrões já são os da TiDB: porta `4000` e TLS obrigatório).

Testes (usam um banco em memória H2, não precisam do MySQL/TiDB): `.\mvnw.cmd test` (ou `./mvnw test`)

## Bom saber

- **Plano grátis do Render "dorme"** após ~15 min sem uso. O primeiro acesso depois disso demora ~30–60 s para acordar; depois fica rápido.
- **Fuso horário:** os horários são salvos em UTC e exibidos no horário do celular. O filtro "Este mês" usa `America/Recife` (mude com a variável `APP_TIMEZONE`).
- **Sem PIN?** Se deixar `APP_PIN` vazio, qualquer pessoa com a URL consegue ver e lançar gastos.
