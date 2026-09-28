# Controle de Gastos

App simples para registrar gastos pelo celular. Cada pessoa cria sua própria conta; você digita o valor, e o sistema salva a data e a hora sozinho.

**Tecnologias:** Java 21 + Spring Boot 3 · HTML/CSS/JS puro · MySQL (TiDB Cloud) · login com JWT + e-mail via Brevo (SMTP)

## Estrutura

```
src/main/java/com/silvano/gastos/
├── model/Usuario.java, Gasto.java      → tabelas "usuarios" e "gastos"
├── repository/                         → consultas ao banco
├── service/
│   ├── AuthService                     → cadastro, login, esqueci/resetar senha
│   ├── JwtService                      → gera/valida o token de sessão
│   ├── EmailService                    → envia o e-mail de redefinição de senha
│   └── GastoService                    → regras dos gastos (sempre por usuário)
├── controller/
│   ├── AuthController                  → API REST (/api/auth)
│   ├── GastoController                 → API REST (/api/gastos)
│   └── GlobalExceptionHandler          → converte erros em JSON {"erro": "..."}
└── config/AuthFilter                   → exige token válido nas rotas privadas
src/main/resources/static/
├── login.html, cadastro.html, esqueci-senha.html, resetar-senha.html
├── index.html                          → tela de lançar gasto
├── historico.html                      → tela de histórico + filtros
├── css/style.css · js/api.js
```

## API

| Método | Rota | O que faz |
|---|---|---|
| POST | `/api/auth/cadastro` | `{ "nome", "email", "senha" }` → cria a conta e retorna o token |
| POST | `/api/auth/login` | `{ "email", "senha" }` → retorna o token |
| POST | `/api/auth/esqueci-senha` | `{ "email" }` → envia o link de redefinição por e-mail |
| POST | `/api/auth/resetar-senha` | `{ "token", "novaSenha" }` |
| GET | `/api/auth/eu` | dados da conta logada |
| POST | `/api/gastos` | `{ "valor": 12.50, "descricao": "Almoço" }` (a descrição é opcional) |
| GET | `/api/gastos?periodo=MES` | `MES`, `7D`, `15D`, `30D`, `3M`, `6M`, `12M` |
| DELETE | `/api/gastos/{id}` | exclui um gasto (só o dono consegue) |

Rotas de `/api/gastos` e `/api/auth/eu` exigem `Authorization: Bearer <token>`.

---

## 1. Criar o banco no TiDB Cloud (grátis)

1. Crie uma conta em https://tidbcloud.com e crie um cluster **Starter/Serverless**.
2. Clique em **Connect**, gere uma senha e anote: **Host**, **Port (4000)**, **User** e **Password**.
3. No **SQL Editor** do TiDB, rode:
   ```sql
   CREATE DATABASE gastos;
   ```
   As tabelas `usuarios` e `gastos` são criadas automaticamente quando o app sobe.

## 2. Criar o envio de e-mail no Brevo (grátis, para o "esqueci minha senha")

> Por que Brevo e não direto pelo Gmail? Contas do Gmail recém-criadas costumam ser bloqueadas pelo Google ao tentar enviar e-mail por app (erro "Username and Password not accepted"), mesmo com a senha de app certa. O Brevo é feito pra isso, sem essa fricção.

1. Crie uma conta grátis em https://www.brevo.com (pode usar o mesmo e-mail que você já criou pro app, ex: `controledegastosapp@gmail.com`).
2. Vá em **Senders, Domains & Dedicated IPs → Senders**, adicione esse mesmo e-mail como remetente e confirme o link que o Brevo manda pra caixa de entrada dele (não precisa ter domínio próprio, só confirmar o e-mail).
3. Vá em **SMTP & API → SMTP**: lá aparece o **login** (é o seu e-mail) e um botão pra **gerar uma nova chave SMTP** — gere uma e copie.
4. Isso vira, no Render: `MAIL_USERNAME` = o login SMTP mostrado · `MAIL_PASSWORD` = a chave SMTP gerada · `MAIL_FROM` = o e-mail que você verificou como remetente no passo 2 (geralmente igual ao `MAIL_USERNAME`).

## 3. Subir o código no GitHub

```bash
git init
git add .
git commit -m "Controle de gastos"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/controle-gastos.git
git push -u origin main
```

## 4. Publicar no Render (grátis)

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
| `JWT_SECRET` | uma string aleatória longa (ex: gere com `openssl rand -base64 32`) |
| `APP_URL` | `https://seu-app.onrender.com` (a própria URL do serviço no Render) |
| `MAIL_USERNAME` | o login SMTP do Brevo (passo 2) |
| `MAIL_PASSWORD` | a chave SMTP gerada no Brevo (passo 2) |
| `MAIL_FROM` | o e-mail que você verificou como remetente no Brevo |

4. Clique em **Deploy**. Quando terminar, abra a URL `https://seu-app.onrender.com`, crie sua conta em **Criar conta** e chame seus amigos — cada um cria a própria conta e só vê os próprios gastos.

## 5. Deixar como "app" no celular

- **Android (Chrome):** menu ⋮ → *Adicionar à tela inicial*
- **iPhone (Safari):** Compartilhar → *Adicionar à Tela de Início*

O login fica salvo no aparelho depois da primeira vez.

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
   As tabelas são criadas sozinhas quando o app sobe (`spring.jpa.hibernate.ddl-auto=update`).
3. Rode o app apontando para o MySQL local (porta padrão `3306`, sem TLS). O e-mail de "esqueci minha senha" só funciona se você também definir `MAIL_USERNAME`/`MAIL_PASSWORD`/`MAIL_FROM`; sem eles, o resto do app funciona normalmente.

   **PowerShell:**
   ```powershell
   $env:DB_HOST="localhost"; $env:DB_PORT="3306"; $env:DB_NAME="gastos"
   $env:DB_USER="gastos_app"; $env:DB_PASSWORD="uma-senha-forte"; $env:DB_SSL_MODE="PREFERRED"
   $env:JWT_SECRET="qualquer-string-aleatoria-de-32-caracteres-ou-mais"
   .\mvnw.cmd spring-boot:run
   ```
   **Linux/Mac:**
   ```bash
   export DB_HOST=localhost DB_PORT=3306 DB_NAME=gastos DB_USER=gastos_app DB_PASSWORD=uma-senha-forte DB_SSL_MODE=PREFERRED
   export JWT_SECRET=qualquer-string-aleatoria-de-32-caracteres-ou-mais
   ./mvnw spring-boot:run
   ```
4. Abra http://localhost:8080 (ele te leva para a tela de login/cadastro).

### Com TiDB Cloud (igual à produção)

Mesma coisa, mas sem definir `DB_PORT` nem `DB_SSL_MODE` (os padrões já são os da TiDB: porta `4000` e TLS obrigatório).

Testes (usam um banco em memória H2, não precisam do MySQL/TiDB nem do Brevo): `.\mvnw.cmd test` (ou `./mvnw test`)

## Bom saber

- **Plano grátis do Render "dorme"** após ~15 min sem uso. O primeiro acesso depois disso demora ~30–60 s para acordar; depois fica rápido.
- **Fuso horário:** os horários são salvos em UTC e exibidos no horário do celular. O filtro "Este mês" usa `America/Recife` (mude com a variável `APP_TIMEZONE`).
- **Cada gasto pertence a quem criou** — não tem mais PIN único compartilhado; cada pessoa cria sua própria conta e só vê os próprios gastos.
- **`JWT_SECRET`** precisa ser a mesma sempre — se você trocar, todo mundo é deslogado (precisa fazer login de novo).
