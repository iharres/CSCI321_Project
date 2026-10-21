console.log("Script is running");


// Load portfolio
fetch("http://localhost:8080/api/portfolio")
    .then(response => response.json())
    .then(portfolio => {

        console.log(portfolio);


        // Update portfolio summary

        document.getElementById("total-cost").innerHTML =
            "Total Cost: $" + portfolio.totalCost.toFixed(2);

        document.getElementById("current-value").innerHTML =
            "Current Value: $" + portfolio.currentValue.toFixed(2);

        document.getElementById("profit").innerHTML =
            "Profit: $" + portfolio.profit.toFixed(2);


        // Display LEGO sets

        const container = document.getElementById("lego-container");

        container.innerHTML = "";


        portfolio.sets.forEach(set => {

            const legoCard = document.createElement("div");

            legoCard.className = "lego-set";


           legoCard.innerHTML = `
              <h3>${set.name}</h3>
              <p>Set #${set.setNumber}</p>
              <p>Condition: ${set.condition === "NEW" || set.condition === "N" ? "New / Sealed" : "Built / Used"}</p>              <p>Purchase Price: $${set.purchasePrice.toFixed(2)}</p>
              <p>Estimated Value: $${set.currentValue.toFixed(2)}</p>
              <p>Profit: $${set.profit.toFixed(2)}</p>

                <button class="remove-button" data-set-number="${set.setNumber}">
                  Remove
              </button>
            `;


            container.appendChild(legoCard);


            // Remove LEGO set

            legoCard.querySelector(".remove-button").addEventListener("click", () => {

                fetch(`http://localhost:8080/api/sets/${set.setNumber}`, {
                    method: "DELETE"
                })
                    .then(response => {

                        if (!response.ok) {
                            throw new Error("Failed to remove LEGO set");
                        }

                        location.reload();

                    })
                    .catch(error => {

                        console.error("Error removing LEGO set:", error);

                    });

            });

        });

    })
    .catch(error => {

        console.error("Error loading portfolio:", error);

    });


// Add LEGO set

document.getElementById("add-set-button").addEventListener("click", () => {

    const setNumber = document.getElementById("set-number").value;
    const purchasePrice = document.getElementById("purchase-price").value;
    const condition = document.getElementById("condition").value;

    const button = document.getElementById("add-set-button");

    button.innerHTML = "Adding...";
    button.disabled = true;

    const newSet = {
        setNumber: setNumber,
        purchasePrice: Number(purchasePrice),
        condition: condition
    };

    fetch("http://localhost:8080/api/sets", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(newSet)
    })
        .then(response => {

            if (!response.ok) {
                throw new Error("Failed to add LEGO set");
            }

            location.reload();

        })
        .catch(error => {

            console.error("Error adding LEGO set:", error);

            button.innerHTML = "Add Set";
            button.disabled = false;

        });

});