const emailInput = document.getElementById("email");
const usernameInput = document.getElementById("username");
const usernameFeedback = document.getElementById("username-feedback");
const roleSelect = document.getElementById("role");
const yearSelect = document.getElementById("year");
const levelSelect = document.getElementById("level");
const groupSelect = document.getElementById("group");

// ===== Username suggested from the email =====
// jean.dupont@univ.fr -> jdupont (same rule as HandleUserService.generateUsername)
function generateUsername(email) {
  email = email.trim().toLowerCase();

  if (!email.includes("@")) {
    return "";
  }

  const name = email.substring(0, email.indexOf("@"));

  if (name.includes(".")) {
    const parts = name.split(".");
    return parts[0].charAt(0) + parts[1];
  }

  return name;
}

// true once the admin has changed the username by hand:
// from then on, typing the email no longer overwrites it
let usernameEditedByHand =
  usernameInput &&
  emailInput &&
  usernameInput.value.trim() !== "" &&
  usernameInput.value.trim() !== generateUsername(emailInput.value);

function onEmailInput() {
  if (usernameEditedByHand) return;

  usernameInput.value = generateUsername(emailInput.value);
  checkUsername();
}

function onUsernameInput() {
  usernameEditedByHand = usernameInput.value.trim() !== "";
  checkUsername();
}

// ===== Instant check: a warning only, saving is still allowed =====
let checkTimer;

function checkUsername() {
  clearTimeout(checkTimer);

  const username = usernameInput.value.trim();

  if (username.length < 3) {
    showUsernameState(null);
    return;
  }

  // wait until the admin stops typing for 400 ms, to avoid one request per key
  checkTimer = setTimeout(async function () {
    try {
      const response = await fetch(
        "/members/username-check?username=" + encodeURIComponent(username),
        { headers: { Accept: "application/json" } },
      );

      if (!response.ok) {
        showUsernameState(null);
        return;
      }

      const result = await response.json();

      // the admin kept typing in the meantime: ignore this old answer
      if (usernameInput.value.trim() !== username) return;

      showUsernameState(result);
    } catch (error) {
      showUsernameState(null);
    }
  }, 400);
}

function showUsernameState(result) {
  usernameInput.classList.remove("is-valid", "border-warning");
  usernameFeedback.textContent = "";
  usernameFeedback.className = "small";

  if (!result) return;

  if (result.available) {
    usernameInput.classList.add("is-valid");
    usernameFeedback.classList.add("text-success");
    usernameFeedback.textContent = "This username is available.";
    return;
  }

  // already used: warning + suggestion, but the form can still be submitted
  usernameInput.classList.add("border-warning");
  usernameFeedback.classList.add("text-warning-emphasis");
  usernameFeedback.textContent =
    "This username is already used. It is better to add a number at the end: ";

  const suggestionButton = document.createElement("button");
  suggestionButton.type = "button";
  suggestionButton.className = "btn btn-link btn-sm p-0 align-baseline";
  suggestionButton.textContent = "use " + result.suggestion;
  suggestionButton.addEventListener("click", function () {
    usernameInput.value = result.suggestion;
    usernameEditedByHand = true;
    checkUsername();
  });

  usernameFeedback.appendChild(suggestionButton);
}

// ===== Academic year, level and group: only for students =====
function updateStudentFields() {
  if (!roleSelect) return;

  const isStudent = roleSelect.value === "ETUDIANT";

  [yearSelect, levelSelect, groupSelect].forEach(function (field) {
    if (!field) return;

    field.disabled = !isStudent;
    field.required = isStudent;

    if (!isStudent) {
      field.value = "";
    }
  });
}

if (emailInput && usernameInput && usernameFeedback) {
  emailInput.addEventListener("input", onEmailInput);
  usernameInput.addEventListener("input", onUsernameInput);
}

if (roleSelect) {
  roleSelect.addEventListener("change", updateStudentFields);
}

// when the form is shown again after an error, the fields are already filled
if (usernameInput && usernameFeedback && usernameInput.value.trim() !== "") {
  checkUsername();
}
updateStudentFields();
