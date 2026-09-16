fetch("http://localhost:8080/api/sets")
    .then(response => response.json())
    .then(sets => {

        const container = document.getElementById("lego-container");


        sets.forEach(set => {

            const legoCard = document.createElement("div");

            legoCard.className = "lego-set";


            legoCard.innerHTML = `
                <h3>${set.name}</h3>
                <p>Set #${set.setNumber}</p>
                <p>Purchase Price: $${set.purchasePrice}</p>
                <p>Current Value: $${set.currentValue}</p>
                <p>Profit: $${set.profit}</p>
            `;


            container.appendChild(legoCard);

        });

    })
    .catch(error => {

        console.error("Error loading LEGO sets:", error);

    });