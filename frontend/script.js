console.log("Script is running");


// ======================================================
// API CONFIGURATION
// ======================================================

const API_HOST =
    window.location.hostname || "localhost";

const API_URL =
    `http://${API_HOST}:8080`;

console.log(
    "Backend API:",
    API_URL
);


// ======================================================
// CSRF SECURITY
// ======================================================

let csrfToken = null;
let csrfHeaderName = null;


/*
 * Get a CSRF token from Spring Security.
 */
async function refreshCsrfToken() {

    try {

        const response = await fetch(
            `${API_URL}/api/csrf`,
            {
                method: "GET",
                credentials: "include"
            }
        );


        if (!response.ok) {

            throw new Error(
                "Could not retrieve CSRF token"
            );
        }


        const csrf =
            await response.json();


        csrfToken =
            csrf.token;

        csrfHeaderName =
            csrf.headerName;


        console.log(
            "CSRF token loaded"
        );


        return true;


    } catch (error) {

        console.error(
            "CSRF error:",
            error
        );


        csrfToken = null;
        csrfHeaderName = null;


        return false;
    }

}


/*
 * Create headers containing the current CSRF token.
 */
function getCsrfHeaders() {

    if (!csrfToken || !csrfHeaderName) {

        return {};
    }


    return {
        [csrfHeaderName]: csrfToken
    };
}


// ======================================================
// LEGO BOX IMAGE SCANNER - GEMINI
// ======================================================

document
    .getElementById("scan-image-button")
    .addEventListener("click", async () => {

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


        if (!csrfToken) {

            await refreshCsrfToken();
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

                    credentials: "include",

                    headers: {
                        ...getCsrfHeaders()
                    },

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
// ACCOUNT ELEMENTS
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


// ======================================================
// REMOVE OLD LOCAL STORAGE LOGIN DATA
// ======================================================

localStorage.removeItem("userId");
localStorage.removeItem("username");


// ======================================================
// CHECK CURRENT LOGIN SESSION
// ======================================================

async function checkLogin() {

    try {

        const response = await fetch(
            `${API_URL}/api/session/me`,
            {
                method: "GET",
                credentials: "include"
            }
        );


        console.log(
            "/api/session/me status:",
            response.status
        );


        if (!response.ok) {

            loginButton.style.display =
                "block";

            loginDropdown.style.display =
                "none";

            userArea.style.display =
                "none";

            return false;
        }


        const user =
            await response.json();


        console.log(
            "Current logged-in user:",
            user
        );


        loginButton.style.display =
            "none";

        loginDropdown.style.display =
            "none";

        userArea.style.display =
            "block";

        usernameButton.textContent =
            user.username;


        return true;


    } catch (error) {

        console.error(
            "Could not check login session:",
            error
        );


        loginButton.style.display =
            "block";

        loginDropdown.style.display =
            "none";

        userArea.style.display =
            "none";


        return false;
    }

}


// ======================================================
// LOGIN DROPDOWN
// ======================================================

loginButton.addEventListener(
    "click",
    () => {

        if (
            loginDropdown.style.display === "none" ||
            loginDropdown.style.display === ""
        ) {

            loginDropdown.style.display =
                "block";

        } else {

            loginDropdown.style.display =
                "none";
        }

    }
);


// ======================================================
// USER DROPDOWN
// ======================================================

usernameButton.addEventListener(
    "click",
    () => {

        if (
            userDropdown.style.display === "none" ||
            userDropdown.style.display === ""
        ) {

            userDropdown.style.display =
                "block";

        } else {

            userDropdown.style.display =
                "none";
        }

    }
);


// ======================================================
// LOGIN
// ======================================================

document
    .getElementById("submit-login")
    .addEventListener("click", async () => {

        const username =
            document
                .getElementById("login-username")
                .value
                .trim();

        const password =
            document.getElementById(
                "login-password"
            ).value;

        const message =
            document.getElementById(
                "login-message"
            );


        if (!username || !password) {

            message.textContent =
                "Enter your username and password.";

            return;
        }


        if (!csrfToken) {

            const csrfLoaded =
                await refreshCsrfToken();


            if (!csrfLoaded) {

                message.textContent =
                    "Security initialization failed.";

                return;
            }
        }


        message.textContent =
            "Logging in...";


        try {

            const response = await fetch(
                `${API_URL}/api/session/login`,
                {
                    method: "POST",

                    credentials: "include",

                    headers: {
                        "Content-Type":
                            "application/json",

                        ...getCsrfHeaders()
                    },

                    body: JSON.stringify({
                        username: username,
                        password: password
                    })
                }
            );


            console.log(
                "Login response status:",
                response.status
            );


            if (!response.ok) {

                throw new Error(
                    "Invalid username or password"
                );
            }


            const user =
                await response.json();


            console.log(
                "Login successful:",
                user
            );


            /*
             * Authentication changed.
             * Get a fresh CSRF token for
             * the authenticated session.
             */
            await refreshCsrfToken();


            const loggedIn =
                await checkLogin();


            if (!loggedIn) {

                throw new Error(
                    "Login succeeded, but the session could not be saved."
                );
            }


            message.textContent =
                "Login successful!";


            document.getElementById(
                "login-password"
            ).value = "";


            loginDropdown.style.display =
                "none";


            await loadPortfolio();


        } catch (error) {

            console.error(
                "Login error:",
                error
            );


            message.textContent =
                error.message;
        }

    });


// ======================================================
// REGISTER
// ======================================================

document
    .getElementById("submit-register")
    .addEventListener("click", async () => {

        const username =
            document
                .getElementById("register-username")
                .value
                .trim();

        const password =
            document.getElementById(
                "register-password"
            ).value;

        const message =
            document.getElementById(
                "register-message"
            );


        if (!username || !password) {

            message.textContent =
                "Enter a username and password.";

            return;
        }


        if (!csrfToken) {

            const csrfLoaded =
                await refreshCsrfToken();


            if (!csrfLoaded) {

                message.textContent =
                    "Security initialization failed.";

                return;
            }
        }


        try {

            const response = await fetch(
                `${API_URL}/api/register`,
                {
                    method: "POST",

                    credentials: "include",

                    headers: {
                        "Content-Type":
                            "application/json",

                        ...getCsrfHeaders()
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


            console.log(
                "Registered user:",
                user
            );


            message.textContent =
                "Account created! You can now log in.";


            document.getElementById(
                "register-password"
            ).value = "";


        } catch (error) {

            console.error(
                "Registration error:",
                error
            );


            message.textContent =
                error.message;
        }

    });


// ======================================================
// LOGOUT
// ======================================================

document
    .getElementById("logout-button")
    .addEventListener("click", async () => {

        try {

            if (!csrfToken) {

                await refreshCsrfToken();
            }


            const response = await fetch(
                `${API_URL}/api/session/logout`,
                {
                    method: "POST",

                    credentials: "include",

                    headers: {
                        ...getCsrfHeaders()
                    }
                }
            );


            console.log(
                "Logout status:",
                response.status
            );


            /*
             * Logout invalidates the authenticated
             * session, so obtain a fresh anonymous
             * CSRF token.
             */
            csrfToken = null;
            csrfHeaderName = null;

            await refreshCsrfToken();


        } catch (error) {

            console.error(
                "Logout error:",
                error
            );
        }


        userDropdown.style.display =
            "none";


        loginButton.style.display =
            "block";

        userArea.style.display =
            "none";


        location.reload();

    });


// ======================================================
// LOAD PORTFOLIO
// ======================================================

async function loadPortfolio() {

    try {

        const response = await fetch(
            `${API_URL}/api/portfolio`,
            {
                method: "GET",
                credentials: "include"
            }
        );


        console.log(
            "Portfolio status:",
            response.status
        );


        if (
            response.status === 401 ||
            response.status === 403
        ) {

            console.log(
                "User is not logged in."
            );

            return;
        }


        if (!response.ok) {

            throw new Error(
                "Failed to load portfolio"
            );
        }


        const portfolio =
            await response.json();


        console.log(
            "Portfolio:",
            portfolio
        );


        document
            .getElementById("total-cost")
            .innerHTML =
                "Total Cost: $" +
                portfolio.totalCost.toFixed(2);


        document
            .getElementById("current-value")
            .innerHTML =
                "Current Value: $" +
                portfolio.currentValue.toFixed(2);


        document
            .getElementById("profit")
            .innerHTML =
                "Profit: $" +
                portfolio.profit.toFixed(2);


        const container =
            document.getElementById(
                "lego-container"
            );


        container.innerHTML =
            "";


        portfolio.sets.forEach(
            set => {

                const legoCard =
                    document.createElement(
                        "div"
                    );


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
                        class="history-button"
                        data-id="${set.id}">
                        Price History
                    </button>

                    <button
                        class="remove-button"
                        data-id="${set.id}">
                        Remove
                    </button>
                `;


                container.appendChild(
                    legoCard
                );

                const historyButton =
                    legoCard.querySelector(
                        ".history-button"
                    );


                historyButton.addEventListener(
                    "click",
                    () => {

                        openPriceHistory(
                            set
                        );
                    }
                );

                const removeButton =
                    legoCard.querySelector(
                        ".remove-button"
                    );


                removeButton.addEventListener(
                    "click",
                    async () => {

                        try {

                            if (!csrfToken) {

                                await refreshCsrfToken();
                            }


                            removeButton.disabled =
                                true;

                            removeButton.textContent =
                                "Removing...";


                            const response =
                                await fetch(
                                    `${API_URL}/api/sets/${set.id}`,
                                    {
                                        method: "DELETE",

                                        credentials:
                                            "include",

                                        headers: {
                                            ...getCsrfHeaders()
                                        }
                                    }
                                );


                            if (
                                response.status === 401 ||
                                response.status === 403
                            ) {

                                alert(
                                    "Your session expired or the security token is invalid. Please log in again."
                                );

                                await checkLogin();

                                return;
                            }


                            if (!response.ok) {

                                throw new Error(
                                    "Failed to remove LEGO set"
                                );
                            }


                            await loadPortfolio();


                        } catch (error) {

                            console.error(
                                "Error removing LEGO set:",
                                error
                            );


                        } finally {

                            removeButton.disabled =
                                false;

                            removeButton.textContent =
                                "Remove";
                        }

                    }
                );

            }
        );


    } catch (error) {

        console.error(
            "Error loading portfolio:",
            error
        );
    }

}


// ======================================================
// ADD LEGO SET
// ======================================================

document
    .getElementById("add-set-button")
    .addEventListener("click", async () => {

        const setNumber =
            document
                .getElementById("set-number")
                .value
                .trim();

        const purchasePrice =
            document
                .getElementById("purchase-price")
                .value;

        const condition =
            document
                .getElementById("condition")
                .value;

        const button =
            document.getElementById(
                "add-set-button"
            );


        if (!setNumber) {

            alert(
                "Please enter or scan a LEGO set number."
            );

            return;
        }


        if (!purchasePrice) {

            alert(
                "Please enter the purchase price."
            );

            return;
        }


        const loggedIn =
            await checkLogin();


        if (!loggedIn) {

            alert(
                "Please log in before adding a LEGO set."
            );

            return;
        }


        if (!csrfToken) {

            await refreshCsrfToken();
        }


        button.textContent =
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


        try {

            const response = await fetch(
                `${API_URL}/api/sets`,
                {
                    method: "POST",

                    credentials: "include",

                    headers: {
                        "Content-Type":
                            "application/json",

                        ...getCsrfHeaders()
                    },

                    body:
                        JSON.stringify(newSet)
                }
            );


            console.log(
                "Add set status:",
                response.status
            );


            if (
                response.status === 401 ||
                response.status === 403
            ) {

                alert(
                    "Your session expired or the security token is invalid. Please log in again."
                );

                await checkLogin();

                return;
            }


            if (!response.ok) {

                throw new Error(
                    "Failed to add LEGO set"
                );
            }


            document
                .getElementById("set-number")
                .value = "";


            document
                .getElementById("purchase-price")
                .value = "";


            document
                .getElementById("scan-status")
                .textContent = "";


            await loadPortfolio();


        } catch (error) {

            console.error(
                "Error adding LEGO set:",
                error
            );


            alert(
                "There was a problem adding the LEGO set."
            );


        } finally {

            button.textContent =
                "Add Set";

            button.disabled =
                false;
        }

    });

// ======================================================
// PRICE HISTORY CHART
// ======================================================

let priceHistoryChart = null;

let selectedHistorySet = null;


async function openPriceHistory(set) {

    selectedHistorySet =
        set;


    const modal =
        document.getElementById(
            "price-history-modal"
        );


    document.getElementById(
        "price-history-title"
    ).textContent =
        `${set.name} — Price History`;


    modal.style.display =
        "flex";


    document
        .querySelectorAll(
            ".history-period-button"
        )
        .forEach(button => {

            button.classList.remove(
                "active"
            );

            if (
                button.dataset.period === "6M"
            ) {

                button.classList.add(
                    "active"
                );
            }
        });


    await loadPriceHistory(
        set.id,
        "6M"
    );
}


// ======================================================
// LOAD PRICE HISTORY
// ======================================================

async function loadPriceHistory(
        setId,
        period) {

    const message =
        document.getElementById(
            "price-history-message"
        );


    const changeDisplay =
        document.getElementById(
            "price-history-change"
        );


    message.textContent =
        "Loading price history...";


    changeDisplay.textContent =
        "";


    try {

        const response = await fetch(
            `${API_URL}/api/sets/${setId}/history?period=${period}`,
            {
                method: "GET",
                credentials: "include"
            }
        );


        if (!response.ok) {

            throw new Error(
                "Could not load price history."
            );
        }


        const history =
            await response.json();


        console.log(
            "Price history:",
            history
        );


        if (
            !history ||
            history.length === 0
        ) {

            message.textContent =
                "No sales history is available for this period.";


            if (priceHistoryChart) {

                priceHistoryChart.destroy();

                priceHistoryChart =
                    null;
            }


            return;
        }


        message.textContent =
            "";


        renderPriceHistoryChart(
            history
        );


        updatePriceChange(
            history
        );


    } catch (error) {

        console.error(
            "Price history error:",
            error
        );


        message.textContent =
            "There was a problem loading price history.";
    }
}


// ======================================================
// RENDER PRICE HISTORY GRAPH
// ======================================================

function renderPriceHistoryChart(
        history) {

    const canvas =
        document.getElementById(
            "price-history-chart"
        );


    const labels =
        history.map(point => {

            const date =
                new Date(
                    point.date + "T00:00:00"
                );


            return date.toLocaleDateString(
                undefined,
                {
                    month: "short",
                    day: "numeric"
                }
            );
        });


    const values =
        history.map(
            point => point.value
        );


    const firstValue =
        values[0];

    const lastValue =
        values[
            values.length - 1
        ];


    const lineColor = "#16a34a";

    const fillColor = "rgba(22, 163, 74, 0.08)";


    if (priceHistoryChart) {

        priceHistoryChart.destroy();
    }


    priceHistoryChart =
        new Chart(
            canvas,
            {

                type: "line",

                data: {

                    labels: labels,

                    datasets: [
                        {

                            label:
                                "Estimated Market Value",

                            data:
                                values,

                            borderColor:
                                lineColor,

                            backgroundColor:
                                fillColor,

                            fill:
                                true,

                            tension:
                                0.25,

                            pointRadius:
                                4,

                            pointHoverRadius:
                                7,

                            borderWidth:
                                3
                        }
                    ]
                },


                options: {

                    responsive:
                        true,

                    maintainAspectRatio:
                        false,

                    interaction: {
                        intersect: false,
                        mode: "index"
                    },

                    plugins: {

                        legend: {
                            display: false
                        },

                        tooltip: {

                            callbacks: {

                                label:
                                    function(context) {

                                        return (
                                            "$" +
                                            context.parsed.y
                                                .toFixed(2)
                                        );
                                    }
                            }
                        }
                    },

scales: {

    x: {

        grid: {
            display: false
        },

        border: {
            display: false
        },

        ticks: {
            color: "#6b7280"
        }
    },


    y: {

        border: {
            display: false
        },

        grid: {
            color: "#f0f0f0"
        },

        ticks: {

            color: "#6b7280",

            callback:
                function(value) {

                    return (
                        "$" +
                        value
                    );
                }
        }
    }
}
                }
            }
        );
}


// ======================================================
// PRICE CHANGE
// ======================================================

function updatePriceChange(
        history) {

    const changeDisplay =
        document.getElementById(
            "price-history-change"
        );


    if (history.length < 2) {

        changeDisplay.textContent =
            "Not enough data to calculate change.";

        return;
    }


    const firstValue =
        history[0].value;


    const lastValue =
        history[
            history.length - 1
        ].value;


    const dollarChange =
        lastValue - firstValue;


    const percentChange =
        (
            dollarChange /
            firstValue
        ) * 100;


    const sign =
        dollarChange >= 0
            ? "+"
            : "";


    changeDisplay.textContent =
        `${sign}$${dollarChange.toFixed(2)} (${sign}${percentChange.toFixed(2)}%)`;


    changeDisplay.style.color =
        dollarChange >= 0
            ? "#22c55e"
            : "#ef4444";
}


// ======================================================
// PERIOD BUTTONS
// ======================================================

document
    .querySelectorAll(
        ".history-period-button"
    )
    .forEach(button => {

        button.addEventListener(
            "click",
            async () => {

                if (!selectedHistorySet) {
                    return;
                }


                document
                    .querySelectorAll(
                        ".history-period-button"
                    )
                    .forEach(otherButton => {

                        otherButton.classList.remove(
                            "active"
                        );
                    });


                button.classList.add(
                    "active"
                );


                await loadPriceHistory(
                    selectedHistorySet.id,
                    button.dataset.period
                );
            }
        );
    });


// ======================================================
// CLOSE PRICE HISTORY
// ======================================================

document
    .getElementById(
        "close-price-history"
    )
    .addEventListener(
        "click",
        () => {

            document.getElementById(
                "price-history-modal"
            ).style.display =
                "none";
        }
    );


document
    .getElementById(
        "price-history-modal"
    )
    .addEventListener(
        "click",
        event => {

            if (
                event.target.id ===
                "price-history-modal"
            ) {

                event.currentTarget.style.display =
                    "none";
            }
        }
    );
// ======================================================
// PORTFOLIO HISTORY
// ======================================================

let portfolioHistoryChart = null;


// ======================================================
// LOAD PORTFOLIO HISTORY
// ======================================================

async function loadPortfolioHistory(period = "6M") {

    const message =
        document.getElementById(
            "portfolio-history-message"
        );


    const changeDisplay =
        document.getElementById(
            "portfolio-history-change"
        );


    message.textContent =
        "Loading portfolio history...";


    changeDisplay.textContent =
        "";


    try {

        const response = await fetch(
            `${API_URL}/api/portfolio/history?period=${period}`,
            {
                method: "GET",
                credentials: "include"
            }
        );


        if (!response.ok) {

            throw new Error(
                "Could not load portfolio history."
            );
        }


        const history =
            await response.json();


        console.log(
            "Portfolio history:",
            history
        );


        if (
            !history ||
            history.length === 0
        ) {

            message.textContent =
                "No portfolio history is available yet.";


            if (portfolioHistoryChart) {

                portfolioHistoryChart.destroy();

                portfolioHistoryChart =
                    null;
            }


            return;
        }


        message.textContent =
            "";


        renderPortfolioHistoryChart(
            history
        );


        updatePortfolioHistoryChange(
            history
        );


    } catch (error) {

        console.error(
            "Portfolio history error:",
            error
        );


        message.textContent =
            "There was a problem loading portfolio history.";
    }
}


// ======================================================
// RENDER PORTFOLIO GRAPH
// ======================================================

function renderPortfolioHistoryChart(
        history) {

    const canvas =
        document.getElementById(
            "portfolio-history-chart"
        );


    const labels =
        history.map(point => {

            const date =
                new Date(
                    point.date + "T00:00:00"
                );


            return date.toLocaleDateString(
                undefined,
                {
                    month: "short",
                    day: "numeric"
                }
            );
        });


    const values =
        history.map(
            point => point.value
        );


    const firstValue =
        values[0];


    const lastValue =
        values[
            values.length - 1
        ];


        const lineColor =
            "#16a34a";


    const fillColor =
        "rgba(22, 163, 74, 0.08)";  


    if (portfolioHistoryChart) {

        portfolioHistoryChart.destroy();
    }


    portfolioHistoryChart =
        new Chart(
            canvas,
            {

                type: "line",

                data: {

                    labels: labels,

                    datasets: [
                        {

                            label:
                                "Portfolio Value",

                            data:
                                values,

                            borderColor:
                                lineColor,

                            backgroundColor:
                                fillColor,

                            fill:
                                true,

                            tension:
                                0.25,

                            pointRadius:
                                3,

                            pointHoverRadius:
                                7,

                            borderWidth:
                                3
                        }
                    ]
                },


                options: {

                    responsive:
                        true,

                    maintainAspectRatio:
                        false,

                    interaction: {

                        intersect:
                            false,

                        mode:
                            "index"
                    },


                    plugins: {

                        legend: {
                            display: false
                        },


                        tooltip: {

                            callbacks: {

                                label:
                                    function(context) {

                                        return (
                                            "Portfolio Value: $" +
                                            context.parsed.y
                                                .toFixed(2)
                                        );
                                    }
                            }
                        }
                    },


            scales: {

                  x: {
                    grid: {
                     display: false
                  },

               border: {
                    display: false
                },

                ticks: {
                   color: "#6b7280"
                   }
     },


    y: {

        border: {
            display: false
        },

        grid: {
            color: "#f0f0f0"
        },

        ticks: {

            color: "#6b7280",

            callback:
                function(value) {

                    return (
                        "$" +
                        value
                    );
                }
                }
             }
            }
                }
            }
        );
}


// ======================================================
// PORTFOLIO CHANGE
// ======================================================

function updatePortfolioHistoryChange(
        history) {

    const changeDisplay =
        document.getElementById(
            "portfolio-history-change"
        );


    if (history.length < 2) {

        changeDisplay.textContent =
            "Not enough history to calculate change.";

        return;
    }


    const firstValue =
        history[0].value;


    const lastValue =
        history[
            history.length - 1
        ].value;


    const dollarChange =
        lastValue - firstValue;


    const percentChange =
        firstValue === 0
            ? 0
            : (
                dollarChange /
                firstValue
            ) * 100;


    const sign =
        dollarChange >= 0
            ? "+"
            : "";


    changeDisplay.textContent =
        `${sign}$${dollarChange.toFixed(2)} (${sign}${percentChange.toFixed(2)}%)`;


    changeDisplay.style.color =
        dollarChange >= 0
            ? "#22c55e"
            : "#ef4444";
}


// ======================================================
// PORTFOLIO PERIOD BUTTONS
// ======================================================

document
    .querySelectorAll(
        ".portfolio-period-button"
    )
    .forEach(button => {

        button.addEventListener(
            "click",
            async () => {

                document
                    .querySelectorAll(
                        ".portfolio-period-button"
                    )
                    .forEach(otherButton => {

                        otherButton.classList.remove(
                            "active"
                        );
                    });


                button.classList.add(
                    "active"
                );


                await loadPortfolioHistory(
                    button.dataset.period
                );
            }
        );
    });

// ======================================================
// START APPLICATION
// ======================================================

async function startApplication() {

    /*
     * CSRF must be loaded before any POST,
     * PUT or DELETE operation can occur.
     */
    await refreshCsrfToken();


    const loggedIn =
        await checkLogin();


   if (loggedIn) {

    await loadPortfolio();

    await loadPortfolioHistory(
        "6M"
    );
    } else {

        console.log(
            "No active login session."
        );
    }

}


startApplication();