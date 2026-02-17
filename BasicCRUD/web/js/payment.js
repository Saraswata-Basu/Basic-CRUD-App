window.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const total = urlParams.get('total');
    
    if (total) {
        document.getElementById('total-display').textContent = `Total Amount: ₹${total}`;
        document.getElementById('total-input').value = total;   // NEW
    }
});