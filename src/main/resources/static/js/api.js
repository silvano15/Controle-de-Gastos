// Funções compartilhadas por todas as páginas

const TOKEN_KEY = "gastos_token";

function getToken() {
  try { return localStorage.getItem(TOKEN_KEY) || ""; } catch { return ""; }
}
function setToken(token) {
  try { localStorage.setItem(TOKEN_KEY, token); } catch { /* ignore */ }
}
function limparToken() {
  try { localStorage.removeItem(TOKEN_KEY); } catch { /* ignore */ }
}

/** Se não estiver logado, manda pra tela de login. Chame no topo das páginas protegidas. */
function protegerPagina() {
  if (!getToken()) window.location.href = "/login.html";
}

/** fetch com o token de sessão no cabeçalho. Se a sessão expirou, manda pro login. */
async function api(url, options = {}) {
  const token = getToken();
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (token) headers["Authorization"] = "Bearer " + token;

  const res = await fetch(url, { ...options, headers });

  if (res.status === 401 && !url.startsWith("/api/auth/")) {
    limparToken();
    window.location.href = "/login.html";
    return new Promise(() => {}); // trava a execução, já estamos saindo da página
  }
  if (!res.ok) {
    let msg = "Erro ao comunicar com o servidor";
    try { msg = (await res.json()).erro || msg; } catch { /* ignore */ }
    throw new Error(msg);
  }
  return res.status === 204 ? null : res.json();
}

const moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
function formatarMoeda(v) { return moeda.format(Number(v)); }

let toastTimer;
function toast(msg, erro = false) {
  const t = document.getElementById("toast");
  t.textContent = msg;
  t.classList.toggle("erro", erro);
  t.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove("show"), 2200);
}
