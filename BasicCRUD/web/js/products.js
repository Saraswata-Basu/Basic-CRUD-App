// Load products and render them
async function loadProducts() {
    try {
        const response = await fetch('/products/api');
        const data = await response.json();
        const products = data.products || [];
        const container = document.getElementById('products-container');
        
        container.innerHTML = '';
        
        products.forEach(product => {
            const div = document.createElement('div');
            div.className = 'product';
            div.innerHTML = `
                <div class="info">
                    <div class="name">${product.name}</div>
                    <div class="meta">&#8377;${product.price} | Stock: ${product.stock}</div>
                </div>
                <div class="quantity-control">
                    <label style="font-size:12px;color:#6b7280;">
                        Qty: <input type="number" 
                                    id="qty-${product.id}" 
                                    min="0" 
                                    max="${product.stock}" 
                                    value="0"
                                    onchange="updateCart('${product.id}', this.value)">
                    </label>
                </div>
            `;
            container.appendChild(div);
        });
        
        // Load current cart quantities
        loadCartQuantities();
    } catch (error) {
        console.error('Error loading products:', error);
    }
}

// Update cart when quantity changes
async function updateCart(productId, quantity) {
    const qty = parseInt(quantity) || 0;
    
    if (qty > 0) {
        // Add/update in cart
        await fetch(`/cart?update=${productId}&qty=${qty}`);
    } else {
        // Remove from cart
        await fetch(`/cart?remove=${productId}`);
    }
    
    // Visual feedback
    const input = document.getElementById(`qty-${productId}`);
    if (input) {
        input.style.background = '#d1fae5';
        setTimeout(() => {
            input.style.background = '';
        }, 300);
    }
}

// Load current cart quantities and update inputs
async function loadCartQuantities() {
    try {
        const response = await fetch('/cart/api');
        const data = await response.json();
        
        data.items.forEach(item => {
            const input = document.getElementById(`qty-${item.id}`);
            if (input) {
                input.value = item.quantity;
            }
        });
    } catch (error) {
        console.error('Error loading cart:', error);
    }
}

// Load products on page load
window.addEventListener('DOMContentLoaded', loadProducts);