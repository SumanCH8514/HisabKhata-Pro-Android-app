# HisabKhata Pro — Database & Business Logic

## 1. Accounting Conventions & Mathematical Formulas

In HisabKhata Pro:
1. **Balance Signs**:
   - `balance < 0`: Merchant is owed money by the customer (**You'll Get** / Receivable). Display in RED with `Math.abs(balance)`.
   - `balance > 0`: Merchant owes money to the customer (**You'll Give** / Advance). Display in GREEN with `balance`.
   - `balance == 0`: Account is settled. Display in GRAY.

2. **Transaction Entry Math**:
   - **Gave (Credit Given / You Gave)**:
     - Entered amount $A > 0$.
     - Recorded transaction amount: $-A$ (Negative).
     - Customer balance update: $\text{Balance}_{\text{new}} = \text{Balance}_{\text{current}} - A$.
   - **Got (Payment Received / You Got)**:
     - Entered amount $A > 0$.
     - Recorded transaction amount: $+A$ (Positive).
     - Customer balance update: $\text{Balance}_{\text{new}} = \text{Balance}_{\text{current}} + A$.

3. **Dashboard Aggregate Totals**:
   - $\text{Total You'll Get} = \sum |\text{balance}_i| \quad \forall \text{ customers where } \text{balance}_i < 0$
   - $\text{Total You'll Give} = \sum \text{balance}_i \quad \forall \text{ customers where } \text{balance}_i > 0$
   - $\text{Net Position} = \text{Total You'll Get} - \text{Total You'll Give}$

4. **Running Ledger Balances**:
   - For a sequence of transactions sorted in reverse chronological order (newest first), the newest transaction reflects the customer's current balance.
   - For preceding transactions:
     $$\text{RunningBalance}_{k-1} = \text{RunningBalance}_k - \text{Amount}_k$$

5. **Precision & Decimal Representation**:
   - All arithmetic must use `BigDecimal` with 2 decimal places and `RoundingMode.HALF_UP`.
   - No floating point arithmetic (`Float` or `Double`) is used for money calculations.

## 2. Idempotency & Conflict Resolution

- Offline transactions generate a UUID `clientTxId`.
- The Outbox table tracks sync state: `PENDING_SYNC`, `SYNCING`, `SYNCED`, `FAILED`, `NEEDS_REVIEW`.
- If an existing transaction with the same `clientTxId` or exact timestamp + amount exists in the cloud, the write is reconciled without duplication.
- Deletions are tombstoned locally until confirmed by the server.
