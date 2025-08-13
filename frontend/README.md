# TRMS AI Frontend

A professional React frontend for the Treasury and Risk Management System (TRMS) AI Assistant. This application provides a Google-like search interface that transitions smoothly into a chat interface for natural language interactions with financial systems.

## Features

### 🎯 Core Features
- **Google-like Search Interface**: Clean, modern landing page with large search input
- **Smooth Animations**: Framer Motion powered transitions between views
- **Professional Chat Interface**: Financial-grade message bubbles and formatting
- **Real-time Communication**: WebSocket support for live chat updates
- **Fallback Support**: Graceful degradation with mock responses when backend unavailable

### 💰 Financial-Specific Features
- **Currency Formatting**: Professional display of monetary amounts with proper localization
- **Account Status Indicators**: Visual status badges for account states (Active, Pending, etc.)
- **Transaction Status Tracking**: Clear indicators for transaction processing states
- **EOD Readiness Dashboard**: Comprehensive End-of-Day status visualization
- **Market Data Completeness**: Progress indicators for data feed status
- **Rate Fixing Displays**: Professional presentation of interest rate data

### 🎨 UI/UX Features
- **Dark Mode Support**: Complete dark/light theme switching
- **Responsive Design**: Mobile-first design that works on all devices
- **Accessibility**: WCAG compliant with keyboard navigation and screen reader support
- **High Contrast Support**: Enhanced visibility for accessibility needs
- **Print Optimization**: Financial report friendly print styles

### 🔧 Technical Features
- **TypeScript Ready**: Type definitions prepared for enhanced development
- **API Integration**: Axios-based HTTP client with comprehensive error handling
- **WebSocket Client**: Real-time bidirectional communication
- **Environment Configuration**: Flexible environment-based configuration
- **Health Monitoring**: Backend connectivity status and health checks

## Demo Scenarios

The interface is designed to handle these key TRMS scenarios:

### Account Management
```
Query: "Show me all USD accounts"
Result: Formatted account listing with balances, status indicators, and timestamps
```

### Balance Inquiries
```
Query: "What's the balance for ACC-001-USD?"
Result: Detailed balance information with currency formatting and transaction history
```

### Transaction Processing
```
Query: "Transfer $50,000 from ACC-001-USD to ACC-002-USD"
Result: Transaction confirmation with ID, status tracking, and updated balances
```

### EOD Processing
```
Query: "Can we run End-of-Day?"
Result: Comprehensive readiness assessment with:
- Market data completeness indicators
- Transaction validation status
- Missing rate fixing identification
- Actionable recommendations
```

### Rate Fixing Proposals
```
Query: "Propose missing rate fixings"
Result: AI-generated rate proposals with:
- Market-based recommendations
- Confidence indicators
- Impact analysis
- Approval workflows
```

## Project Structure

```
src/
├── components/
│   ├── FinancialFormatters.jsx    # Currency, status, and data formatting components
│   └── MessageFormatters.jsx      # Enhanced message display with financial context
├── services/
│   └── api.js                     # API client with WebSocket and HTTP support
├── App.jsx                        # Main application with view management
├── main.jsx                       # Application entry point
└── index.css                      # Enhanced CSS with financial styling
```

## Quick Start

### Prerequisites
- Node.js 18+ and npm
- TRMS AI Backend running on http://localhost:8080 (optional - has fallback)

### Installation & Development
```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Environment Configuration
Create `.env` file or modify existing one:
```env
VITE_API_URL=http://localhost:8080
VITE_WS_URL=http://localhost:8080
VITE_DEV_MODE=true
VITE_ENABLE_MOCK_FALLBACK=true
```

## API Integration

### Backend Health Check
The frontend automatically checks backend connectivity and displays status:
- 🟢 **Connected**: Backend available, using live AI responses
- 🟡 **Disconnected**: Backend unavailable, using enhanced mock responses

### WebSocket Support
Enable real-time chat by toggling WebSocket mode in the connection status bar. WebSocket provides:
- Instant message delivery
- Real-time typing indicators
- Live status updates
- Reduced latency for financial operations

### Mock Response System
When the backend is unavailable, the frontend provides sophisticated mock responses that demonstrate all key features:
- Realistic account data with multiple currencies
- Complex EOD readiness scenarios
- Transaction processing workflows
- Rate fixing proposals with market data

## Accessibility Features

### Keyboard Navigation
- Tab navigation through all interactive elements
- Enter/Space activation for buttons and suggestions
- Escape to close modals and overlays

### Screen Reader Support
- Semantic HTML structure
- ARIA labels for complex financial data
- Status announcements for dynamic content
- Alternative text for visual indicators

### Visual Accessibility
- High contrast mode support
- Scalable text and UI elements
- Focus indicators for all interactive elements
- Color-blind friendly status indicators

## Performance Optimizations

### Code Splitting
- Lazy loading of financial components
- Dynamic imports for large dependencies
- Route-based code splitting ready

### Caching Strategy
- API response caching (15-minute self-cleaning cache)
- Static asset optimization
- Service worker ready architecture

### Bundle Optimization
- Tree shaking for unused code
- Minification and compression
- Modern JavaScript output with fallbacks

## Development Guidelines

### Component Architecture
- Functional components with hooks
- Separated concerns (data, presentation, logic)
- Reusable financial formatting utilities
- Consistent error boundary handling

### State Management
- React hooks for local state
- Context for global state (connection status, theme)
- Optimistic updates for better UX
- Error recovery patterns

### Styling Approach
- Tailwind CSS for rapid development
- CSS custom properties for theming
- Component-scoped styles when needed
- Responsive-first design principles

## Production Deployment

### Build Optimization
```bash
# Production build with optimizations
npm run build

# Analyze bundle size
npx vite-bundle-analyzer dist/
```

### Environment Setup
Configure production environment variables:
```env
VITE_API_URL=https://your-backend-domain.com
VITE_WS_URL=wss://your-websocket-domain.com
VITE_DEV_MODE=false
```

### Docker Deployment
```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

## Contributing

### Code Style
- ESLint configuration for consistent code style
- Prettier for automatic code formatting
- Conventional commits for clear history

### Testing
```bash
# Run tests
npm test

# Run tests with coverage
npm run test:coverage

# Run E2E tests
npm run test:e2e
```

### Financial Data Handling
- Always use proper currency formatting
- Include proper decimal precision for financial amounts
- Implement proper rounding for calculations
- Use ISO currency codes consistently

## Browser Support

- **Modern Browsers**: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- **Mobile Browsers**: iOS Safari 14+, Chrome Mobile 90+
- **Graceful Degradation**: Basic functionality in older browsers

## Security Considerations

- **Input Sanitization**: All user inputs properly sanitized
- **XSS Prevention**: Content Security Policy headers
- **API Security**: HTTPS-only in production
- **Data Privacy**: No sensitive financial data cached in browser

---

## Support

For technical support or questions about the TRMS AI Frontend:

1. Check the backend connectivity status in the app
2. Review browser console for any errors
3. Verify environment configuration
4. Test with mock responses to isolate backend issues

**Note**: This frontend is designed to work independently with sophisticated mock responses, making it perfect for demos and development when the backend is not available.