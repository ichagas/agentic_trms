from flask import Blueprint, jsonify, request
from src.models.account import Account, Transaction, db
import uuid
from decimal import Decimal

trms_bp = Blueprint('trms', __name__)

@trms_bp.route('/accounts', methods=['GET'])
def get_accounts():
    """Get all accounts or filter by currency"""
    currency = request.args.get('currency')
    if currency:
        accounts = Account.query.filter_by(currency=currency.upper()).all()
    else:
        accounts = Account.query.all()
    return jsonify([account.to_dict() for account in accounts])

@trms_bp.route('/accounts/<account_id>/balance', methods=['GET'])
def get_account_balance(account_id):
    """Get account balance"""
    account = Account.query.get_or_404(account_id)
    return jsonify({
        'account_id': account.id,
        'balance': float(account.balance),
        'currency': account.currency,
        'status': account.status
    })

@trms_bp.route('/transactions', methods=['POST'])
def create_transaction():
    """Create a new transaction"""
    data = request.json
    
    # Validate required fields
    required_fields = ['from_account', 'to_account', 'amount', 'currency']
    for field in required_fields:
        if field not in data:
            return jsonify({'error': f'Missing required field: {field}'}), 400
    
    # Check if accounts exist
    from_account = Account.query.get(data['from_account'])
    to_account = Account.query.get(data['to_account'])
    
    if not from_account:
        return jsonify({'error': 'Source account not found'}), 404
    if not to_account:
        return jsonify({'error': 'Target account not found'}), 404
    
    amount = Decimal(str(data['amount']))
    
    # Check sufficient balance
    if from_account.balance < amount:
        return jsonify({'error': 'Insufficient funds'}), 400
    
    # Create transaction
    transaction = Transaction(
        id=f"TXN-{uuid.uuid4().hex[:8].upper()}",
        from_account=data['from_account'],
        to_account=data['to_account'],
        amount=amount,
        currency=data['currency'].upper(),
        description=data.get('description', ''),
        status='Completed'
    )
    
    # Update balances
    from_account.balance -= amount
    to_account.balance += amount
    
    db.session.add(transaction)
    db.session.commit()
    
    return jsonify(transaction.to_dict()), 201

@trms_bp.route('/transactions', methods=['GET'])
def get_transactions():
    """Get all transactions"""
    transactions = Transaction.query.order_by(Transaction.created_at.desc()).all()
    return jsonify([transaction.to_dict() for transaction in transactions])

@trms_bp.route('/transactions/<transaction_id>', methods=['GET'])
def get_transaction(transaction_id):
    """Get specific transaction"""
    transaction = Transaction.query.get_or_404(transaction_id)
    return jsonify(transaction.to_dict())

@trms_bp.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'TRMS Mock System',
        'version': '1.0.0'
    })

