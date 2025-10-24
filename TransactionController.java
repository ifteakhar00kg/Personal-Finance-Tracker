import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    // Use an in-memory List as the data store
    private final List<Transaction> transactions = new ArrayList<>();
    // Use AtomicLong for generating thread-safe unique IDs
    private final AtomicLong idCounter = new AtomicLong();

    // Constructor to pre-populate some data for testing
    public TransactionController() {
        transactions.add(new Transaction(idCounter.incrementAndGet(), TransactionType.INCOME, 1500.00, "Salary", LocalDate.now().minusDays(1)));
        transactions.add(new Transaction(idCounter.incrementAndGet(), TransactionType.EXPENSE, 75.50, "Groceries", LocalDate.now().minusDays(1)));
        transactions.add(new Transaction(idCounter.incrementAndGet(), TransactionType.EXPENSE, 12.00, "Coffee", LocalDate.now()));
    }

    /**
     * Basic Endpoint 1: GET /api/transactions
     * Advanced Endpoint: GET /api/transactions?type=income
     * * This single method handles both requirements.
     * It uses the Stream API for the optional filtering.
     */
    @GetMapping
    public List<Transaction> getTransactions(@RequestParam(required = false) String type) {
        // Check if the 'type' parameter is provided
        if (type != null && !type.isEmpty()) {
            // Use Stream API to filter by type (as requested in assignment)
            return transactions.stream()
                    .filter(t -> t.getType().name().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        // If no type is specified, return all transactions
        return transactions;
    }

    /**
     * Basic Endpoint 2: GET /api/transactions/{id}
     * * Uses Stream API to find the specific transaction.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        // Use Stream API to find the first transaction matching the ID
        Optional<Transaction> transaction = transactions.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();

        // Return the transaction with 200 OK, or 404 Not Found
        return transaction.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Basic Endpoint 5: POST /api/transactions
     */
    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction newTransaction) {
        // Generate a new ID and set it on the object
        newTransaction.setId(idCounter.incrementAndGet());
        // Add the new transaction to the list
        transactions.add(newTransaction);
        // Return 201 Created with the new transaction in the body
        return ResponseEntity.status(HttpStatus.CREATED).body(newTransaction);
    }

    /**
     * Basic Endpoint 3: PUT /api/transactions/{id}
     * * This method follows the approach from your screenshot:
     * 1. It uses a `for-each` loop to find the object.
     * 2. It updates the fields of the object found in the list.
     * 3. It returns the updated object.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction updatedTransaction) {
        
        Transaction transactionToUpdate = null;

        // Using a for-each loop to find the transaction, as per your image
        for (Transaction t : transactions) {
            if (t.getId().equals(id)) {
                transactionToUpdate = t;
                break; // Found it, stop the loop
            }
        }

        // Check if we found the transaction
        if (transactionToUpdate != null) {
            // Update the fields of the object *in the list*
            transactionToUpdate.setType(updatedTransaction.getType());
            transactionToUpdate.setAmount(updatedTransaction.getAmount());
            transactionToUpdate.setDescription(updatedTransaction.getDescription());
            transactionToUpdate.setDate(updatedTransaction.getDate());
            
            // Return 200 OK with the updated object
            return ResponseEntity.ok(transactionToUpdate);
        } else {
            // If the loop finishes without finding it, return 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Basic Endpoint 4: DELETE /api/transactions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        // Use removeIf (which uses a Predicate, part of functional programming)
        boolean removed = transactions.removeIf(t -> t.getId().equals(id));

        if (removed) {
            // Return 204 No Content (standard for successful delete)
            return ResponseEntity.noContent().build();
        } else {
            // Return 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
}
