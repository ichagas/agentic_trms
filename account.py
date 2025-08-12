from src.models.user import db
from datetime import datetime

class Account(db.Model):
    __tablename__ = 'accounts'
    
    id = db.Column(db.String(50), primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    currency = db.Column(db.String(3), nullable=False)
    account_type = db.Column(db.String(50), nullable=False)
    status = db.Column(db.String(20), nullable=False, default='Active')
    balance = db.Column(db.Numeric(15, 2), nullable=False, default=0.00)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'currency': self.currency,
            'account_type': self.account_type,
            'status': self.status,
            'balance': float(self.balance),
            'created_at': self.created_at.isoformat() if self.created_at else None
        }

class Transaction(db.Model):
    __tablename__ = 'transactions'
    
    id = db.Column(db.String(50), primary_key=True)
    from_account = db.Column(db.String(50), db.ForeignKey('accounts.id'), nullable=False)
    to_account = db.Column(db.String(50), db.ForeignKey('accounts.id'), nullable=False)
    amount = db.Column(db.Numeric(15, 2), nullable=False)
    currency = db.Column(db.String(3), nullable=False)
    description = db.Column(db.String(255))
    status = db.Column(db.String(20), nullable=False, default='Completed')
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'id': self.id,
            'from_account': self.from_account,
            'to_account': self.to_account,
            'amount': float(self.amount),
            'currency': self.currency,
            'description': self.description,
            'status': self.status,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }

