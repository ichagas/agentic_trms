The Hackathon proposal is: 
 - Project: NextAgent Command Center (NACC) 
 - Description: Where Legacy Meets Intelligence: AI Agents That Speak Banking Fluently. Our goal is to simplify operations like checking EOD readiness or validating transactions which requires extensive manual effort and knowledge. We are going build AI agents to bridge this gap by understanding natural language requests and orchestrating multi-step operations across legacy systems, turning hours of manual work into simple conversations.

Let's work on the presentation script and review the scenarios.
I'm thinking about the main scenario to be about "Ready to run EOD". User will ask if both system are ready for EOD and will resolve the open issues using AI Chat bot help. 
We might have between 5 to 7 mins to present the demo:
  0:30 - Intro: main page (chat), TRMS and SWIFT dashboards
  0:30 - Simple inquiries, no real action for audience adaptation
  0:30 - Refresh page and starting script: Asking if EOD is ready ion both systems
  0:30 - Book a CASH transaction transfering cash from 2 accounts
  0:30 - Linking SWIFT transaction to resolve UNRECONCILED status (use previous transaction ID)
  0:30 - Another ask if EOD is ready
  0:30 - Resolve the TRMS missing resets
  0:30 - Resolve the SWIFT missing reports
  0:30 - Final ask if EOD is ready
  0:30 - SWIFT inquiry about the redemption report
  0:30 - TBD
  0:30 - TBD
  0:30 - TBD
  0:30 - TBD
  5:00 - Q&A and Discussion

Read docs/AI_FUNCTIONS_REFERENCE.md and DEMO_SCENARIOS.md to get more details about the scenarios and available functions.

The source code /Users/igorchagas/ideas/agentic_trms/backend/trms-ai-backend/src/main/java/com/trms/ai/service/TrmsAiService.java for the main AI agent orchestration logic.

1) Let's review all the flow
2) Make sure they are working properly and the type of inquiry need to input
3) Make adjustments in the frontend "tips" and "Try these queries", if need

For now, just analyze, review and propose any change before any code. I don't want to change anything that is not need.




  🎬 Optimized Demo Script with Exact Queries

  | Time | Step             | Exact Query                                                      | Expected Outcome                                 |
  |------|------------------|------------------------------------------------------------------|--------------------------------------------------|
  | 0:30 | Intro            | Show dashboards                                                  | Display TRMS and SWIFT dashboards                |
  | 0:30 | Warm-up          | "Show me all USD accounts"                                       | Display account list, audience adaptation        |
  | 0:30 | Main Check       | "Can we run EOD? Check both TRMS and SWIFT readiness"            | Shows TRMS EOD status + suggests checking SWIFT  |
  | 0:30 | Book Transaction | "Transfer $50,000 from ACC-001-USD to ACC-002-USD"               | Creates PENDING transaction (note ID)            |
  | 0:30 | Approve + SWIFT  | 1) Approve in dashboard 2) "Send transaction TXN-XXXXX via SWIFT"| SWIFT MT103 sent, status CONFIRMED               |
  | 0:30 | EOD Check Again  | "Can we run EOD now?"                                            | Shows TRMS issues (missing resets, unreconciled) |
  | 0:30 | Resolve TRMS     | "Propose missing rate fixings"                                   | Shows rate proposals (GBP-3M, EUR-6M, etc.)      |
  | 0:30 | Resolve SWIFT    | "Verify today's EOD reports"                                     | Checks shared drive reports                      |
  | 0:30 | Final EOD        | "Check EOD readiness now"                                        | All clear! ✅                                     |
  | 0:30 | Bonus Demo       | "Process the redemption report"                                  | Shows 96-page automation                         |
  | 0:30 | TBD Option 1     | "Reconcile SWIFT messages automatically"                         | Shows reconciliation workflow                    |
  | 0:30 | TBD Option 2     | "Is SWIFT ready for EOD?"                                        | Shows comprehensive SWIFT validation             |







what if we allow the llm make the decision which function to call instead of a rule-based aogorithm? How hard is to implement it as optional as paramenter? Standard is programatically and 