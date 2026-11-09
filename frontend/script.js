console.log("Script is running");

const API_URL = "http://localhost:8080";

// ======================================================
// LEGO BOX IMAGE SCANNER - GEMINI
// ======================================================

document.getElementById("scan-image-button").addEventListener("click", async () => {

    const imageInput =
        document.getElementById("lego-image");

    const status =
        document.getElementById("scan-status");

    const setNumberInput =
        document.getElementById("set-number");

    if (!imageInput.files.length) {
        status.textContent =
            "Please select an image first.";

        return;
    }

    const image =
        imageInput.files[0];

    status.textContent =
        "Identifying LEGO set...";

    const formData =
        new FormData();

    formData.append(
        "image",
        image
    );

    try {

        const response = await fetch(
            `${API_URL}/api/scan-lego`,
            {
                method: "POST",
                body: formData
            }
        );

        if (!response.ok) {
            throw new Error(
                "Could not identify LEGO set."
            );
        }

        const result =
            await response.json();

        console.log(
            "Gemini result:",
            result
        );

        if (!result.setNumber) {
            status.textContent =
                "Could not identify the LEGO set.";

            return;
        }

        // Automatically fill the set number field
        setNumberInput.value =
            result.setNumber;

        status.textContent =
            `Found: ${result.name} (${result.setNumber})`;

    } catch (error) {

        console.error(
            "Gemini scan error:",
            error
        );

        status.textContent =
            "Error identifying LEGO set.";
    }

});


// ======================================================
// ACCOUNT / LOGIN SYSTEM
// ======================================================

const loginButton =
    document.getElementById("login-button");

const loginDropdown =
    document.getElementById("login-dropdown");

const userArea =
    document.getElementById("user-area");

const usernameButton =
    document.getElementById("username-button");

const userDropdown =
    document.getElementById("user-dropdown");


function checkLogin() {

    const userId =
        localStorage.getItem("userId");

    const username =
        localStorage.getItem("username");

    if (userId && username) {

        loginButton.style.display =
            "none";

        loginDropdown.style.display =
            "none";

        userArea.style.display =
            "block";

        usernameButton.textContent =
            username;

    } else {

        loginButton.style.display =
            "block";

        userArea.style.display =
            "none";
    }

}


// ======================================================
// LOGIN DROPDOWN
// ======================================================

loginButton.addEventListener("click", () => {

    if (loginDropdown.style.display === "none") {

        loginDropdown.style.display =
            "block";

    } else {

        loginDropdown.style.display =
            "none";
    }

});


usernameButton.addEventListener("click", () => {

    if (userDropdown.style.display === "none") {

        userDropdown.style.display =
            "block";

    } else {

        userDropdown.style.display =
            "none";
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

        const response = await fetch(
            `${API_URL}/api/login`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    username: username,
                    password: password
                })
            }
        );

        if (!response.ok) {
            throw new Error(
                "Invalid username or password"
            );
        }

        const user =
            await response.json();

        localStorage.setItem(
            "userId",
            user.id
        );

        localStorage.setItem(
            "username",
            user.username
        );

        message.textContent =
            "Login successful!";

        console.log(
            "Logged in user:",
            user
        );

        checkLogin();

        loadPortfolio();

    } catch (error) {

        message.textContent =
            error.message;
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

        const response = await fetch(
            `${API_URL}/api/register`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    username: username,
                    password: password
                })
            }
        );

        if (!response.ok) {

            if (response.status === 409) {
                throw new Error(
                    "Username already exists"
                );
            }

            throw new Error(
                "Registration failed"
            );
        }

        const user =
            await response.json();

        message.textContent =
            "Account created! You can now log in.";

        console.log(
            "Registered user:",
            user
        );

    } catch (error) {

        message.textContent =
            error.message;
    }

});


// ======================================================
// LOGOUT
// ======================================================

document.getElementById("logout-button").addEventListener("click", () => {

    localStorage.removeItem(
        "userId"
    );

    localStorage.removeItem(
        "username"
    );

    userDropdown.style.display =
        "none";

    checkLogin();

    location.reload();

});


// ======================================================
// LOAD PORTFOLIO
// ======================================================

function loadPortfolio() {

    const userId =
        localStorage.getItem("userId");

    if (!userId) {

        console.log(
            "No user is logged in."
        );

        return;
    }

    fetch(
        `${API_URL}/api/portfolio?userId=${userId}`
    )

        .then(response => {

            if (!response.ok) {
                throw new Error(
                    "Failed to load portfolio"
                );
            }

            return response.json();
        })

        .then(portfolio => {

            console.log(
                portfolio
            );

            document.getElementById("total-cost").innerHTML =
                "Total Cost: $" +
                portfolio.totalCost.toFixed(2);

            document.getElementById("current-value").innerHTML =
                "Current Value: $" +
                portfolio.currentValue.toFixed(2);

            document.getElementById("profit").innerHTML =
                "Profit: $" +
                portfolio.profit.toFixed(2);


            const container =
                document.getElementById("lego-container");

            container.innerHTML =
                "";


            portfolio.sets.forEach(set => {

                const legoCard =
                    document.createElement("div");

                legoCard.className =
                    "lego-set";


                legoCard.innerHTML = `
                    <h3>
                        ${set.name}
                    </h3>

                    <p>
                        Set #${set.setNumber}
                    </p>

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


                container.appendChild(
                    legoCard
                );


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

document.getElementById("add-set-button").addEventListener("click", () => {

    const setNumber =
        document.getElementById("set-number").value;

    const purchasePrice =
        document.getElementById("purchase-price").value;

    const condition =
        document.getElementById("condition").value;

    const userId =
        localStorage.getItem("userId");

    const button =
        document.getElementById("add-set-button");


    if (!userId) {

        alert(
            "Please log in before adding a LEGO set."
        );

        return;
    }


    button.innerHTML =
        "Adding...";

    button.disabled =
        true;


    const newSet = {

        setNumber:
            setNumber,

        purchasePrice:
            Number(purchasePrice),

        condition:
            condition
    };


    fetch(
        `${API_URL}/api/sets?userId=${userId}`,
        {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body:
                JSON.stringify(newSet)

        }
    )

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

            button.innerHTML =
                "Add Set";

            button.disabled =
                false;

        });

});


// ======================================================
// START APPLICATION
// ======================================================

checkLogin();

loadPortfolio();