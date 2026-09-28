// Funções compartilhadas pelas duas páginas

const PIN_KEY = "gastos_pin";

function getPin() {
  try { return localStorage.getItem(PIN_KEY) || ""; } catch { return ""; }
}
function setPin(pin) {
  try { localStorage.setItem(PIN_KEY, pin); } catch { /* ignore */ }
}

/** fetch com PIN no cabeçalho. Se o PIN estiver errado, pede de novo. */
async function api(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: { "Content-Type": "application/json", "X-Pin": getPin(), ...(options.headers || {}) },
  });
  if (res.status === 401) {
    await pedirPin();
    return api(url, options);
  }
  if (!res.ok) {
    let msg = "Erro ao comunicar com o servidor";
    try { msg = (await res.json()).erro || msg; } catch { /* ignore */ }
    throw new Error(msg);
  }
  return res.status === 204 ? null : res.json();
}

/** Mostra a tela de PIN e espera o usuário digitar. */
function pedirPin() {
  return new Promise((resolve) => {
    const overlay = document.getElementById("pin-overlay");
    const form = overlay.querySelector("form");
    const input = overlay.querySelector("input");
    overlay.classList.add("show");
    input.value = "";
    setTimeout(() => input.focus(), 50);
    form.onsubmit = (e) => {
      e.preventDefault();
      setPin(input.value.trim());
      overlay.classList.remove("show");
      resolve();
    };
  });
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
