const emailInput = document.getElementById("email");
const usernameInput = document.getElementById("identifiant");

emailInput.addEventListener("input", () => {
  const email = emailInput.value;
  const localPart = email.split("@")[0];
  const parts = localPart.split(".");

  if (parts.length >= 2) {
    usernameInput.value = parts[0].charAt(0) + parts[1];
  } else {
    usernameInput.value = "";
  }
});
