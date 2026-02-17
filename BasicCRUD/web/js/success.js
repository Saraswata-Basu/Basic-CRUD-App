window.addEventListener('DOMContentLoaded', () => {
    // Auto-download PDF after 1 second
    setTimeout(() => {
        const link = document.createElement('a');
        link.href = '/bill.txt';
        link.download = 'bill.txt';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }, 1000);
});