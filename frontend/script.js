console.log("Script is running");

const API_URL = "http://localhost:8080";


// ======================================================
// ACCOUNT / LOGIN SYSTEM
// ======================================================

const loginButton = document.getElementById("login-button");
const loginDropdown = document.getElementById("login-dropdown");

const userArea = document.getElementById("user-area");
const usernameButton = document.getElementById("username-button");
const userDropdown = document.getElementById("user-dropdown");


// Check if someone is already logged in

function checkLogin() {

    const userId = localStorage.getItem("userId");
    const username = localStorage.getItem("username");

    if (userId && username) {

        // User is logged in

        loginButton.style.display = "none";
        loginDropdown.style.display = "none";

        userArea.style.display = "block";

        usernameButton.textContent = username;

    } else {

        // User is logged out

        loginButton.style.display = "block";
        userArea.style.display = "none";

    }
}


// Open login dropdown

loginButton.addEventListener("click", () => {

    if (loginDropdown.style.display === "none") {

        loginDropdown.style.display = "block";

    } else {

        loginDropdown.style.display = "none";

    }

});


// Open username dropdown

usernameButton.addEventListener("click", () => {

    if (userDropdown.style.display === "none") {

        userDropdown.style.display = "block";

    } else {

        userDropdown.style.display = "none";

    }

});


// ======================================================
// LOGIN
// ======================================================

document.getElementById("submit-login").addEventListener("click", async () => {

    const username =
        document.getElementById("login-username").value;

    const password =
        document.getElementById("login-password").value;

    const message =
        document.getElementById("login-message");

    try {

        const response = await fetch(`${API_URL}/api/login`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                username: username,
                password: password
            })

        });

        if (!response.ok) {

            throw new Error("Invalid username or password");

        }

        const user = await response.json();

        // Save user information

        localStorage.setItem("userId", user.id);
        localStorage.setItem("username", user.username);

        message.textContent = "Login successful!";

        // Update account display

        checkLogin();

        // Reload portfolio

        loadPortfolio();

    } catch (error) {

        message.textContent = error.message;

    }

});


// ======================================================
// REGISTER
// ======================================================

document.getElementById("submit-register").addEventListener("click", async () => {

    const username =
        document.getElementById("register-username").value;

    const password =
        document.getElementById("register-password").value;

    const message =
        document.getElementById("register-message");

    try {

        const response = await fetch(`${API_URL}/api/register`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                username: username,
                password: password
            })

        });

        if (!response.ok) {

            if (response.status === 409) {

                throw new Error("Username already exists");

            }

            throw new Error("Registration failed");

        }

        const user = await response.json();

        message.textContent =
            "Account created! You can now log in.";

        console.log("Registered:", user);

    } catch (error) {

        message.textContent = error.message;

    }

});


// ======================================================
// LOGOUT
// ======================================================

document.getElementById("logout-button").addEventListener("click", () => {

    localStorage.removeItem("userId");
    localStorage.removeItem("username");

    userDropdown.style.display = "none";

    checkLogin();

    location.reload();

});


// ======================================================
// LOAD PORTFOLIO
// ======================================================

function loadPortfolio() {

   const userId = localStorage.getItem("userId");

        if (!userId) {
         console.log("No user is logged in.");
             return;
        }

        fetch(`${API_URL}/api/portfolio?userId=${userId}`)

        .then(response => response.json())

        .then(portfolio => {

            console.log(portfolio);


            // Update portfolio summary

            document.getElementById("total-cost").innerHTML =
                "Total Cost: $" +
                portfolio.totalCost.toFixed(2);

            document.getElementById("current-value").innerHTML =
                "Current Value: $" +
                portfolio.currentValue.toFixed(2);

            document.getElementById("profit").innerHTML =
                "Profit: $" +
                portfolio.profit.toFixed(2);


            // Display LEGO sets

            const container =
                document.getElementById("lego-container");

            container.innerHTML = "";


            portfolio.sets.forEach(set => {

                const legoCard =
                    document.createElement("div");

                legoCard.className = "lego-set";


                legoCard.innerHTML = `
                    <h3>${set.name}</h3>

                    <p>Set #${set.setNumber}</p>

                    <p>
                        Condition:
                        ${
                            set.condition === "NEW" ||
                            set.condition === "N"
                                ? "New / Sealed"
                                : "Built / Used"
                        }
                    </p>

                    <p>
                        Purchase Price:
                        $${set.purchasePrice.toFixed(2)}
                    </p>

                    <p>
                        Estimated Value:
                        $${set.currentValue.toFixed(2)}
                    </p>

                    <p>
                        Profit:
                        $${set.profit.toFixed(2)}
                    </p>

                    <button
                        class="remove-button"
                        data-id="${set.id}">
                        Remove
                    </button>
                `;


                container.appendChild(legoCard);


                // Remove LEGO set

                legoCard
                    .querySelector(".remove-button")
                    .addEventListener("click", () => {

                        fetch(
                            `${API_URL}/api/sets/${set.id}`,
                            {
                                method: "DELETE"
                            }
                        )

                        .then(response => {

                            if (!response.ok) {

                                throw new Error(
                                    "Failed to remove LEGO set"
                                );

                            }

                            location.reload();

                        })

                        .catch(error => {

                            console.error(
                                "Error removing LEGO set:",
                                error
                            );

                        });

                    });

            });

        })

        .catch(error => {

            console.error(
                "Error loading portfolio:",
                error
            );

        });

}


// ======================================================
// ADD LEGO SET
// ======================================================

document
    .getElementById("add-set-button")
    .addEventListener("click", () => {

        const setNumber =
            document.getElementById("set-number").value;

        const purchasePrice =
            document.getElementById("purchase-price").value;

        const condition =
            document.getElementById("condition").value;

        const button =
            document.getElementById("add-set-button");


        button.innerHTML = "Adding...";
        button.disabled = true;


        const newSet = {

            setNumber: setNumber,

            purchasePrice: Number(purchasePrice),

            condition: condition

        };


        const userId = localStorage.getItem("userId");

        fetch(`${API_URL}/api/sets?userId=${userId}`, {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(newSet)

        })

        .then(response => {

            if (!response.ok) {

                throw new Error(
                    "Failed to add LEGO set"
                );

            }

            location.reload();

        })

        .catch(error => {

            console.error(
                "Error adding LEGO set:",
                error
            );

            button.innerHTML = "Add Set";

            button.disabled = false;

        });

    });


// ======================================================
// START APPLICATION
// ======================================================

checkLogin();

loadPortfolio();