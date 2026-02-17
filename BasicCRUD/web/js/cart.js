let cartTotal = 0;

async function loadCart() {
    try {
        const response = await fetch('/cart/api');
        const data = await response.json();
        const container = document.getElementById('cart-container');
        const totalSection = document.getElementById('total-section');
        
        if (data.items.length === 0) {
            container.innerHTML = '<div class="empty-cart">Your cart is currently empty.</div>';
            totalSection.innerHTML = '';
            return;
        }
        
        container.innerHTML = '';
        cartTotal = 0;
        
        data.items.forEach(item => {
            const subtotal = item.price * item.quantity;
            cartTotal += subtotal;
            
            const div = document.createElement('div');
            div.className = 'item';
            div.innerHTML = `
                <div class="item-info">
                    <div class="item-name">${item.name}</div>
                    <div class="item-details">
                        Price: &#8377;${item.price} | Subtotal: &#8377;${subtotal.toFixed(2)}
                    </div>
                </div>
                <div class="quantity-control">
                    <label>
                        Qty:
                        <input type="number" 
                            id="cart-qty-${item.id}" 
                            min="0" 
                            value="${item.quantity}"
                            onchange="updateCartItem('${item.id}', this.value)">
                    </label>
                </div>
            `;
            container.appendChild(div);
        });
        
        totalSection.innerHTML = `
            <div class="total">Total: &#8377;${cartTotal.toFixed(2)}</div>
            <a href="/payment?total=${cartTotal.toFixed(2)}" class="button">Proceed to Pay</a>
        `;
    } catch (error) {
        console.error('Error loading cart:', error);
    }
}

async function updateCartItem(productId, quantity) {
    const qty = parseInt(quantity) || 0;
    
    if (qty > 0) {
        await fetch(`/cart?update=${productId}&qty=${qty}`);
    } else {
        await fetch(`/cart?remove=${productId}`);
    }
    
    loadCart(); // Reload cart display
}

window.addEventListener('DOMContentLoaded', loadCart);