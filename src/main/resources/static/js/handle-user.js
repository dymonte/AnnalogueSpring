const emailInput = document.getElementById("email");
const usernameInput = document.getElementById("username");
const roleSelect = document.getElementById("role");
const groupSelect = document.getElementById("group");

// ===== Username generated from the email =====
// jean.dupont@univ.fr -> jdupont (same rule as HandleUserService.generateUsername)
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

// ===== Group disabled for professors =====
function updateGroup() {
  const ishavegroup =
    roleSelect.value === "PROF" || roleSelect.value === "ADMIN";
  
  groupSelect.disabled = ishavegroup;

  if (ishavegroup) {
    // clear the choice: a disabled field is not sent with the form
    groupSelect.value = "";
  }
}

emailInput.addEventListener("input", updateUsername);
roleSelect.addEventListener("change", updateGroup);

// when the form is shown again after an error, the fields are already filled
updateUsername();
updateGroup();
