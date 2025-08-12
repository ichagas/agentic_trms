from src.models.account import Account, Transaction, db
from decimal import Decimal

def seed_accounts():
    """Seed the database with sample accounts"""
    accounts = [
        Account(
            id='ACC-001-USD',
            name='Trading Account USD',
            currency='USD',
            account_type='Trading',
            status='Active',
            balance=Decimal('1000000.00')
        ),
        Account(
            id='ACC-002-USD',
            name='Settlement Account USD',
            currency='USD',
            account_type='Settlement',
            status='Active',
            balance=Decimal('500000.00')
        ),
        Account(
            id='ACC-003-EUR',
            name='Trading Account EUR',
            currency='EUR',
            account_type='Trading',
            status='Active',
            balance=Decimal('750000.00')
        ),
        Account(
            id='ACC-004-EUR',
            name='Settlement Account EUR',
            currency='EUR',
            account_type='Settlement',
            status='Active',
            balance=Decimal('300000.00')
        ),
        Account(
            id='ACC-005-GBP',
            name='Trading Account GBP',
            currency='GBP',
            account_type='Trading',
            status='Active',
            balance=Decimal('600000.00')
        ),
        Account(
            id='ACC-006-JPY',
            name='Trading Account JPY',
            currency='JPY',
            account_type='Trading',
            status='Active',
            balance=Decimal('100000000.00')
        )
    ]
    
    for account in accounts:
        existing = Account.query.get(account.id)
        if not existing:
            db.session.add(account)
    
    db.session.commit()
    print(f"Seeded {len(accounts)} accounts")

def seed_transactions():
    """Seed the database with sample transactions"""
    transactions = [
        Transaction(
            id='TXN-SAMPLE01',
            from_account='ACC-001-USD',
            to_account='ACC-002-USD',
            amount=Decimal('50000.00'),
            currency='USD',
            description='Initial settlement transfer',
            status='Completed'
        ),
        Transaction(
            id='TXN-SAMPLE02',
            from_account='ACC-003-EUR',
            to_account='ACC-004-EUR',
            amount=Decimal('25000.00'),
            currency='EUR',
            description='Daily settlement',
            status='Completed'
        )
    ]
    
    for transaction in transactions:
        existing = Transaction.query.get(transaction.id)
        if not existing:
            db.session.add(transaction)
    
    db.session.commit()
    print(f"Seeded {len(transactions)} transactions")

def seed_all():
    """Seed all data"""
    seed_accounts()
    seed_transactions()
    print("Database seeding completed!")

