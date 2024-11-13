const API_BASE_URL = "http://localhost:8080"; // Update if your backend runs on a different URL or port

document.getElementById("transferForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const recipientId = document.getElementById("recipient").value;
    const amount = document.getElementById("amount").value;

    try {
        const response = await fetch(`${API_BASE_URL}/transfers`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                recipientId: recipientId,
                amount: parseFloat(amount),
            }),
        });

        if (response.ok) {
            document.getElementById("message").textContent = "Transfer successful!";
        } else {
            const errorData = await response.json();
            document.getElementById("message").textContent = `Error: ${errorData.message}`;
        }
    } catch (error) {
        document.getElementById("message").textContent = "Network error: Could not complete transfer.";
        console.error("Error:", error);
    }
});
