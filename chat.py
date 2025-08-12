from flask import Blueprint, jsonify, request
import requests
import json
import uuid
from datetime import datetime

chat_bp = Blueprint('chat', __name__)

# Mock TRMS service URL (will be the mocked TRMS system)
TRMS_BASE_URL = 'http://localhost:5000/api/trms'

# Mock AI responses for different types of queries
def mock_ai_response(message, conversation_id):
    """Mock AI response based on the user message"""
    message_lower = message.lower()
    
    # Account queries
    if 'account' in message_lower and ('usd' in message_lower or 'eur' in message_lower or 'gbp' in message_lower or 'jpy' in message_lower):
        currency = None
        if 'usd' in message_lower:
            currency = 'USD'
        elif 'eur' in message_lower:
            currency = 'EUR'
        elif 'gbp' in message_lower:
            currency = 'GBP'
        elif 'jpy' in message_lower:
            currency = 'JPY'
        
        try:
            response = requests.get(f'{TRMS_BASE_URL}/accounts', params={'currency': currency})
            if response.status_code == 200:
                accounts = response.json()
                if accounts:
                    account_list = "\n".join([f"• {acc['name']} ({acc['id']}) - Status: {acc['status']}" for acc in accounts])
                    return f"I found {len(accounts)} {currency} accounts in the system:\n\n{account_list}"
                else:
                    return f"No {currency} accounts found in the system."
            else:
                return "Sorry, I couldn't retrieve account information at the moment."
        except Exception as e:
            return "Sorry, I'm having trouble connecting to the TRMS system."
    
    # Balance queries
    elif 'balance' in message_lower:
        # Extract account ID if mentioned
        words = message.split()
        account_id = None
        for word in words:
            if 'ACC-' in word.upper():
                account_id = word.upper()
                break
        
        if account_id:
            try:
                response = requests.get(f'{TRMS_BASE_URL}/accounts/{account_id}/balance')
                if response.status_code == 200:
                    balance_info = response.json()
                    return f"Account {balance_info['account_id']} has a balance of {balance_info['balance']:,.2f} {balance_info['currency']}. Status: {balance_info['status']}"
                else:
                    return f"Account {account_id} not found."
            except Exception as e:
                return "Sorry, I couldn't retrieve balance information at the moment."
        else:
            return "Please specify an account ID to check the balance. For example: 'Check balance for ACC-001-USD'"
    
    # Transaction queries
    elif 'transaction' in message_lower or 'transfer' in message_lower:
        if 'create' in message_lower or 'book' in message_lower or 'transfer' in message_lower:
            return "To create a transaction, I need the following information:\n• Source account ID\n• Target account ID\n• Amount\n• Currency\n• Description (optional)\n\nPlease provide these details and I'll help you book the transaction."
        else:
            try:
                response = requests.get(f'{TRMS_BASE_URL}/transactions')
                if response.status_code == 200:
                    transactions = response.json()
                    if transactions:
                        recent_txns = transactions[:5]  # Show last 5 transactions
                        txn_list = "\n".join([f"• {txn['id']}: {txn['amount']} {txn['currency']} from {txn['from_account']} to {txn['to_account']}" for txn in recent_txns])
                        return f"Here are the most recent transactions:\n\n{txn_list}"
                    else:
                        return "No transactions found in the system."
                else:
                    return "Sorry, I couldn't retrieve transaction information at the moment."
            except Exception as e:
                return "Sorry, I'm having trouble connecting to the TRMS system."
    
    # Help queries
    elif 'help' in message_lower or 'what can you do' in message_lower:
        return """I'm your TRMS AI assistant! I can help you with:

🏦 **Account Management**
• View accounts by currency (e.g., "Show me USD accounts")
• Check account balances (e.g., "Check balance for ACC-001-USD")

💰 **Transactions**
• View recent transactions
• Help create new transactions

📊 **Reports & Information**
• Account summaries
• Transaction history

Just ask me in natural language! For example:
• "Show me all EUR accounts"
• "What's the balance of ACC-001-USD?"
• "Show recent transactions"
"""
    
    # Default response
    else:
        return "I'm your TRMS AI assistant. I can help you with account queries, balance checks, transactions, and reports. What would you like to know about your treasury system?"

@chat_bp.route('/chat', methods=['POST'])
def chat():
    """Handle chat messages"""
    try:
        data = request.json
        message = data.get('message', '')
        conversation_id = data.get('conversation_id', str(uuid.uuid4()))
        
        if not message:
            return jsonify({'error': 'Message is required'}), 400
        
        # Mock AI processing
        ai_response = mock_ai_response(message, conversation_id)
        
        response_data = {
            'response': ai_response,
            'conversation_id': conversation_id,
            'timestamp': datetime.utcnow().isoformat(),
            'status': 'success'
        }
        
        return jsonify(response_data)
        
    except Exception as e:
        return jsonify({
            'error': 'Failed to process chat message',
            'details': str(e)
        }), 500

@chat_bp.route('/chat/health', methods=['GET'])
def chat_health():
    """Health check for chat service"""
    return jsonify({
        'status': 'healthy',
        'service': 'TRMS AI Chat Service',
        'version': '1.0.0',
        'trms_connection': 'mocked'
    })

