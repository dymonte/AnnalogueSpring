const emailInput = document.getElementById("email");
const usernameInput = document.getElementById("username");
const roleSelect = document.getElementById("role");
const groupSelect = document.getElementById("group");
const yearInput = document.getElementById("year");

// ===== Username generated from the email =====
// jean.dupont@univ.fr -> jdupont (same rule as HandleUserService.generateUsername)
function updateUsername() {
  if (!emailInput || !usernameInput) return;

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
  if (!roleSelect) return;

  const ishavegroup =
    roleSelect.value === "PROF" || roleSelect.value === "ADMIN";

  if (groupSelect) {
    groupSelect.disabled = ishavegroup;
    groupSelect.required = !ishavegroup;

    if (ishavegroup) {
      groupSelect.value = "";
    }
  }

  if (yearInput) {
    yearInput.disabled = ishavegroup;
    yearInput.required = !ishavegroup;

    if (ishavegroup) {
      yearInput.value = "";
    }
  }
}

if (emailInput) {
  emailInput.addEventListener("input", updateUsername);
}

if (roleSelect) {
  roleSelect.addEventListener("change", updateGroup);
}

// when the form is shown again after an error, the fields are already filled
updateUsername();
updateGroup();
