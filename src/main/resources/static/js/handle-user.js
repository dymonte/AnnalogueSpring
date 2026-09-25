const emailInput = document.getElementById("email");
const usernameInput = document.getElementById("username");

function updateUsername() {
  const email = emailInput.value.trim().toLowerCase();

  if (!email.includes("@")) {
    usernameInput.value = "";
    return;
  }

  const name = email.substring(0, email.indexOf("@"));

  if (name.includes(".")) {
    const parts = name.split(".");
    usernameInput.value = parts[0].charAt(0) + parts[1];
  } else {
    usernameInput.value = name;
  }
}

emailInput.addEventListener("input", updateUsername);

// Generate the username if the email is already filled
// after the form is shown again following an error
updateUsername();
