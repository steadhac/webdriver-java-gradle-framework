

 Here's a new section to add to your document — Java Data Structures & Algorithms, continuing the ShopEasy story:

```markdown
---

## Part 6: Java Data Structures & Algorithms (Bonus)

*The computer science fundamentals behind ShopEasy's code.*

---

### Big O Notation — How fast is your code?

ShopEasy has 1 million products. How you store and search them matters.

```
| Big O       | Name         | 1,000 items | 1,000,000 items | ShopEasy Example                        |
|-------------|--------------|-------------|-----------------|------------------------------------------|
| O(1)        | Constant     | 1 op        | 1 op            | HashMap.get("productId")                 |
| O(log n)    | Logarithmic  | 10 ops      | 20 ops          | Binary search on sorted prices           |
| O(n)        | Linear       | 1,000 ops   | 1,000,000 ops   | Scan all products for a name             |
| O(n log n)  | Linearithmic | 10,000 ops  | 20,000,000 ops  | Sort products by price                   |
| O(n²)       | Quadratic    | 1,000,000   | 1,000,000,000   | Compare every product to every other     |
```

```java
// O(1) — instant, no matter how many products
Map<Integer, Product> catalog = new HashMap<>();
Product p = catalog.get(42);  // one lookup, done

// O(n) — check every item
for (Product p : allProducts) {
    if (p.getName().equals("Laptop")) return p;  // worst case: check all 1M
}

// O(n²) — nested loop, avoid with large data
for (Product a : allProducts) {
    for (Product b : allProducts) {
        if (a.getPrice() == b.getPrice() && a != b)  // find same-priced items
            System.out.println(a.getName() + " = " + b.getName());
    }
}
// 1M × 1M = 1 trillion comparisons. Your laptop catches fire.
```

---

### Array vs ArrayList

```java
// ARRAY — fixed size, set at creation, cannot grow
// Use when you know the exact count upfront

String[] categories = new String[4];  // exactly 4 slots, forever
categories[0] = "Electronics";
categories[1] = "Clothing";
categories[2] = "Books";
categories[3] = "Home";
// categories[4] = "Sports";  // ArrayIndexOutOfBoundsException! No room.

// Accessing by index: O(1) — instant
String first = categories[0];  // "Electronics"


// ARRAYLIST — dynamic size, grows automatically
// Use when the count is unknown or changes

List<String> cart = new ArrayList<>();
cart.add("Laptop");       // size = 1
cart.add("Headphones");   // size = 2
cart.add("Mouse");        // size = 3
cart.remove("Headphones"); // size = 2 — try doing this with an array!

// Accessing by index: O(1) — same as array
String item = cart.get(0);  // "Laptop"

// Searching by value: O(n) — checks each element
boolean hasLaptop = cart.contains("Laptop");  // scans the whole list

// ShopEasy test example: collect all product names from an API response
Response r = api.get("/products");
List<String> names = r.jsonPath().getList("name");  // returns ArrayList
Assert.assertTrue(names.contains("Wireless Headphones"));
Assert.assertEquals(names.size(), 20);  // page has 20 products
```

```
Array:     [Electronics][Clothing][Books][Home]
            Fixed. Cannot add a 5th slot.

ArrayList: [Laptop] → [Laptop][Headphones] → [Laptop][Headphones][Mouse]
            Grows automatically. Shrinks on remove.
```

---

### LinkedList vs ArrayList

```java
// ARRAYLIST — data stored in a contiguous block of memory
// Fast: get by index O(1), add to end O(1)
// Slow: insert/remove in the middle O(n) — shifts everything after it

List<String> recentOrders = new ArrayList<>();
recentOrders.add("ORD-001");       // fast — append to end
recentOrders.add("ORD-002");
recentOrders.add(0, "ORD-000");    // SLOW — shifts ORD-001 and ORD-002 right
recentOrders.get(1);               // FAST — direct index access


// LINKEDLIST — data stored as nodes pointing to each other
// Fast: insert/remove at beginning or middle O(1) if you have the node
// Slow: get by index O(n) — must walk from the start

LinkedList<String> orderQueue = new LinkedList<>();
orderQueue.addFirst("ORD-100");    // fast — just update pointers
orderQueue.addLast("ORD-101");     // fast
orderQueue.removeFirst();           // fast — process the first order in line
orderQueue.get(5);                  // SLOW — walk through 5 nodes to get there
```

```
ArrayList (contiguous memory):
  Index:  [0]       [1]       [2]       [3]
  Data:   [ORD-001] [ORD-002] [ORD-003] [ORD-004]
  Insert at [1]? → Shift [1],[2],[3] right. Expensive.

LinkedList (scattered nodes with pointers):
  [ORD-001] → [ORD-002] → [ORD-003] → [ORD-004]
  Insert after ORD-001? → Just repoint: [ORD-001] → [NEW] → [ORD-002]
  No shifting needed. But finding ORD-003? Walk 3 hops.
```

**When to use each:**
| Scenario | Use |
|----------|-----|
| Read by index frequently | ArrayList |
| Add/remove at the end | ArrayList |
| Add/remove at the beginning or middle frequently | LinkedList |
| Implement a queue (FIFO) | LinkedList |
| Default choice for most cases | ArrayList |

---

### HashMap and HashSet

```java
// HASHMAP — key-value pairs, O(1) lookup by key
// ShopEasy product catalog: product ID → product details

Map<Integer, String> products = new HashMap<>();
products.put(42, "Laptop");
products.put(43, "Headphones");
products.put(44, "Mouse");

String name = products.get(42);          // "Laptop" — O(1), instant
boolean exists = products.containsKey(99); // false — O(1)
products.remove(44);                      // removes Mouse — O(1)

// Real framework usage: building an API request body
Map<String, Object> body = new HashMap<>();
body.put("name", "Wireless Headphones");
body.put("price", 79.99);
body.put("category", "Electronics");
Response r = api.post("/products", body);

// Iterate over all entries
for (Map.Entry<Integer, String> entry : products.entrySet()) {
    System.out.println("Product #" + entry.getKey() + ": " + entry.getValue());
}


// HASHSET — unique values only, O(1) lookup
// ShopEasy: track which categories are used (no duplicates)

Set<String> categories = new HashSet<>();
categories.add("Electronics");
categories.add("Clothing");
categories.add("Electronics");  // duplicate — ignored silently
System.out.println(categories.size());  // 2, not 3

// Real test usage: verify no duplicate product IDs in API response
Response r = api.get("/products");
List<Integer> ids = r.jsonPath().getList("id");
Set<Integer> uniqueIds = new HashSet<>(ids);
Assert.assertEquals(ids.size(), uniqueIds.size(), "Duplicate product IDs found!");
```

```
HashMap (key → value):
  ┌────────────────────────────────────┐
  │ Key(42) → "Laptop"                │  Bucket 2
  │ Key(43) → "Headphones"            │  Bucket 3
  │ Key(44) → "Mouse"                 │  Bucket 4
  └────────────────────────────────────┘
  How? hashCode(42) decides which bucket → go straight there → O(1)

HashSet (values only, no duplicates):
  { "Electronics", "Clothing", "Books" }
  Internally it's a HashMap where each value is a key with a dummy value.
```

---

### Stack (LIFO — Last In, First Out)

```java
// ShopEasy browser back-button history
// The last page you visited is the first one you go back to

Stack<String> browserHistory = new Stack<>();
browserHistory.push("/home");           // visit home
browserHistory.push("/products");       // visit products
browserHistory.push("/products/42");    // visit a specific product
browserHistory.push("/cart");           // visit cart

// User clicks "Back"
String currentPage = browserHistory.pop();   // "/cart" — removed
String previousPage = browserHistory.peek(); // "/products/42" — still in stack

// Back again
browserHistory.pop();   // "/products/42" removed
browserHistory.peek();  // "/products"

System.out.println(browserHistory);  // [/home, /products]
```

```
Push order:         Pop order (reverse):
  /home               /cart          ← comes out first (LIFO)
  /products            /products/42
  /products/42         /products
  /cart     ← top      /home         ← comes out last
```

```java
// Classic interview problem: validate matched brackets
// ShopEasy uses JSON — every { must have a matching }

public boolean isValidJson(String s) {
    Stack<Character> stack = new Stack<>();
    Map<Character, Character> pairs = Map.of(')', '(', '}', '{', ']', '[');

    for (char c : s.toCharArray()) {
        if (c == '(' || c == '{' || c == '[') {
            stack.push(c);  // opening bracket → push
        } else if (pairs.containsKey(c)) {
            if (stack.isEmpty() || stack.pop() != pairs.get(c)) {
                return false;  // no match → invalid
            }
        }
    }
    return stack.isEmpty();  // all brackets matched?
}

isValidJson("{\"name\": \"Laptop\", \"tags\": [\"electronics\"]}");  // true
isValidJson("{\"name\": \"Laptop\"");  // false — missing closing }
```

---

### Queue (FIFO — First In, First Out)

```java
// ShopEasy order processing — first order placed = first order shipped

Queue<String> orderQueue = new LinkedList<>();
orderQueue.add("ORD-001");    // customer A places order
orderQueue.add("ORD-002");    // customer B places order
orderQueue.add("ORD-003");    // customer C places order

// Process orders in the order they were placed
String next = orderQueue.poll();   // "ORD-001" — removed and returned
String peek = orderQueue.peek();   // "ORD-002" — looked at but NOT removed
next = orderQueue.poll();          // "ORD-002"
next = orderQueue.poll();          // "ORD-003"
next = orderQueue.poll();          // null — queue is empty
```

```
Add order:                    Process (poll):
  ORD-001 → ORD-002 → ORD-003    ORD-001 comes out first (FIFO)
  (front)              (back)     then ORD-002, then ORD-003
```

```java
// PriorityQueue — elements come out in priority order, not insertion order
// ShopEasy: VIP orders get processed before regular orders

Queue<int[]> orderPriority = new PriorityQueue<>(
    Comparator.comparingInt(a -> a[0])  // sort by priority number (lower = higher priority)
);
orderPriority.add(new int[]{3, 101});  // regular order #101
orderPriority.add(new int[]{1, 102});  // VIP order #102
orderPriority.add(new int[]{2, 103});  // premium order #103

orderPriority.poll();  // [1, 102] — VIP processed first
orderPriority.poll();  // [2, 103] — premium second
orderPriority.poll();  // [3, 101] — regular last
```

---

### Binary Search — O(log n)

```java
// ShopEasy has 1 million products sorted by price.
// Find the product priced at $79.99.

// LINEAR SEARCH — O(n): check every product. Up to 1,000,000 checks.
for (int i = 0; i < sortedPrices.length; i++) {
    if (sortedPrices[i] == 79.99) return i;  // worst case: check all
}

// BINARY SEARCH — O(log n): cut the list in half each time. ~20 checks for 1M items.
public int binarySearch(double[] prices, double target) {
    int left = 0;
    int right = prices.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;  // avoid integer overflow

        if (prices[mid] == target) {
            return mid;             // found it
        } else if (prices[mid] < target) {
            left = mid + 1;         // target is in the right half
        } else {
            right = mid - 1;        // target is in the left half
        }
    }
    return -1;  // not found
}
```

```
Searching for $79.99 in 16 sorted prices:

Step 1: [10, 20, 30, 40, 50, 60, 70, |80|, 90, 100, 200, 300, 400, 500, 600, 700]
        mid = 80. Target 79.99 < 80 → search LEFT half

Step 2: [10, 20, 30, |40|, 50, 60, 70]
        mid = 40. Target 79.99 > 40 → search RIGHT half

Step 3: [50, |60|, 70]
        mid = 60. Target 79.99 > 60 → search RIGHT half

Step 4: [|70|]
        mid = 70. Target 79.99 > 70 → left > right → not found (closest is 80)

4 steps instead of 16. For 1,000,000 items: ~20 steps instead of 1,000,000.
```

```java
// Java has built-in binary search:
double[] prices = {9.99, 19.99, 29.99, 49.99, 79.99, 99.99, 199.99};
int index = Arrays.binarySearch(prices, 79.99);  // returns 4

List<Double> priceList = List.of(9.99, 19.99, 29.99, 49.99, 79.99);
int index = Collections.binarySearch(priceList, 79.99);  // returns 4

// REQUIREMENT: the array/list MUST be sorted. Binary search on unsorted data = wrong answers.
```

---

### Sorting Algorithms

```java
// ShopEasy needs to sort products by price for the "Low to High" filter.

double[] prices = {79.99, 19.99, 199.99, 49.99, 9.99};
// Goal: {9.99, 19.99, 49.99, 79.99, 199.99}


// --- BUBBLE SORT — O(n²) — simple but slow ---
// Repeatedly swap adjacent elements if they're in the wrong order.
// Like bubbles rising to the surface.

public void bubbleSort(double[] arr) {
    for (int i = 0; i < arr.length - 1; i++) {
        for (int j = 0; j < arr.length - 1 - i; j++) {
            if (arr[j] > arr[j + 1]) {
                double temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}
```

```
Bubble Sort step-by-step on [79.99, 19.99, 199.99, 49.99, 9.99]:

Pass 1: [79.99, 19.99] → swap → [19.99, 79.99, 199.99, 49.99, 9.99]
         [79.99, 199.99] → ok
         [199.99, 49.99] → swap → [19.99, 79.99, 49.99, 199.99, 9.99]
         [199.99, 9.99]  → swap → [19.99, 79.99, 49.99, 9.99, 199.99]
         199.99 is now in its final position (bubbled to the end)

Pass 2: [19.99, 79.99] → ok
         [79.99, 49.99] → swap → [19.99, 49.99, 79.99, 9.99, 199.99]
         [79.99, 9.99]  → swap → [19.99, 49.99, 9.99, 79.99, 199.99]

Pass 3: [19.99, 49.99] → ok
         [49.99, 9.99]  → swap → [19.99, 9.99, 49.99, 79.99, 199.99]

Pass 4: [19.99, 9.99]  → swap → [9.99, 19.99, 49.99, 79.99, 199.99] ✓
```

```java
// --- SELECTION SORT — O(n²) — find the minimum, put it first ---

public void selectionSort(double[] arr) {
    for (int i = 0; i < arr.length - 1; i++) {
        int minIdx = i;
        for (int j = i + 1; j < arr.length; j++) {
            if (arr[j] < arr[minIdx]) minIdx = j;
        }
        double temp = arr[minIdx];
        arr[minIdx] = arr[i];
        arr[i] = temp;
    }
}
```

```
Selection Sort on [79.99, 19.99, 199.99, 49.99, 9.99]:

Step 1: Find minimum in entire array → 9.99 at index 4
        Swap with index 0 → [9.99, 19.99, 199.99, 49.99, 79.99]

Step 2: Find minimum in [19.99, 199.99, 49.99, 79.99] → 19.99 at index 1
        Already in place → [9.99, 19.99, 199.99, 49.99, 79.99]

Step 3: Find minimum in [199.99, 49.99, 79.99] → 49.99 at index 3
        Swap with index 2 → [9.99, 19.99, 49.99, 199.99, 79.99]

Step 4: Find minimum in [199.99, 79.99] → 79.99 at index 4
        Swap with index 3 → [9.99, 19.99, 49.99, 79.99, 199.99] ✓
```

```java
// --- MERGE SORT — O(n log n) — divide and conquer, stable ---
// Split the array in half, sort each half, merge them back.

public void mergeSort(double[] arr, int left, int right) {
    if (left >= right) return;  // base case: 1 element is already sorted

    int mid = left + (right - left) / 2;
    mergeSort(arr, left, mid);       // sort left half
    mergeSort(arr, mid + 1, right);  // sort right half
    merge(arr, left, mid, right);    // merge the two sorted halves
}

private void merge(double[] arr, int left, int mid, int right) {
    double[] temp = new double[right - left + 1];
    int i = left, j = mid + 1, k = 0;

    while (i <= mid && j <= right) {
        if (arr[i] <= arr[j]) temp[k++] = arr[i++];
        else                  temp[k++] = arr[j++];
    }
    while (i <= mid)   temp[k++] = arr[i++];
    while (j <= right) temp[k++] = arr[j++];

    System.arraycopy(temp, 0, arr, left, temp.length);
}
```

```
Merge Sort on [79.99, 19.99, 199.99, 49.99, 9.99]:

Split:   [79.99, 19.99, 199.99, 49.99, 9.99]
              /                    \
    [79.99, 19.99, 199.99]    [49.99, 9.99]
        /          \              /       \
  [79.99, 19.99]  [199.99]  [49.99]    [9.99]
    /       \
 [79.99]  [19.99]

Merge back up:
 [79.99] + [19.99] → [19.99, 79.99]         (compare, smaller first)
 [19.99, 79.99] + [199.99] → [19.99, 79.99, 199.99]
 [49.99] + [9.99] → [9.99, 49.99]
 [19.99, 79.99, 199.99] + [9.99, 49.99] → [9.99, 19.99, 49.99, 79.99, 199.99] ✓
```

```java
// --- In practice, just use Java's built-in sort (Timsort, O(n log n)) ---
double[] prices = {79.99, 19.99, 199.99, 49.99, 9.99};
Arrays.sort(prices);  // [9.99, 19.99, 49.99, 79.99, 199.99]

// Sort objects by a field
List<Product> products = getProducts();
products.sort(Comparator.comparingDouble(Product::getPrice));           // low to high
products.sort(Comparator.comparingDouble(Product::getPrice).reversed()); // high to low

// Sort API response for verification
List<Double> apiPrices = response.jsonPath().getList("price");
List<Double> sorted = new ArrayList<>(apiPrices);
Collections.sort(sorted);
Assert.assertEquals(apiPrices, sorted, "Products should be sorted by price");
```

| Algorithm | Time (Best) | Time (Worst) | Space | Stable? | When to Use |
|-----------|-------------|-------------|-------|---------|-------------|
| Bubble Sort | O(n) | O(n²) | O(1) | Yes | Never in production. Learning only. |
| Selection Sort | O(n²) | O(n²) | O(1) | No | Never in production. Learning only. |
| Merge Sort | O(n log n) | O(n log n) | O(n) | Yes | Need guaranteed O(n log n) + stability |
| Quick Sort | O(n log n) | O(n²) | O(log n) | No | General purpose, fast in practice |
| Arrays.sort() | O(n log n) | O(n log n) | O(n) | Yes | Always. Java uses Timsort (hybrid). |

---

### Recursion

```java
// A function that calls itself, breaking a problem into smaller pieces.
// ShopEasy: calculate the total discount for nested coupon codes.

// SIMPLE: factorial — 5! = 5 × 4 × 3 × 2 × 1 = 120
public int factorial(int n) {
    if (n <= 1) return 1;       // BASE CASE — stop here
    return n * factorial(n - 1); // RECURSIVE CASE — call yourself with a smaller problem
}

// How it works:
// factorial(5)
//   → 5 * factorial(4)
//     → 4 * factorial(3)
//       → 3 * factorial(2)
//         → 2 * factorial(1)
//           → 1  (base case hit, start returning)
//         → 2 * 1 = 2
//       → 3 * 2 = 6
//     → 4 * 6 = 24
//   → 5 * 24 = 120


// PRACTICAL: ShopEasy has nested categories.
// Electronics → Audio → Headphones → Wireless Headphones
// Find all products in a category and its subcategories.

public List<Product> getAllProducts(Category category) {
    List<Product> result = new ArrayList<>(category.getProducts());  // this category's products

    for (Category sub : category.getSubCategories()) {
        result.addAll(getAllProducts(sub));  // recursively get subcategory products
    }

    return result;
}

// Category tree:
//   Electronics (3 products)
//   ├── Audio (5 products)
//   │   ├── Headphones (8 products)
//   │   └── Speakers (4 products)
//   └── Computers (10 products)
//
// getAllProducts(Electronics) → 3 + 5 + 8 + 4 + 10 = 30 products


// FIBONACCI — classic interview question
// Each number is the sum of the two before it: 0, 1, 1, 2, 3, 5, 8, 13...
public int fibonacci(int n) {
    if (n <= 0) return 0;       // base case
    if (n == 1) return 1;       // base case
    return fibonacci(n - 1) + fibonacci(n - 2);  // two recursive calls
}

// WARNING: naive fibonacci is O(2^n) — extremely slow for large n
// fibonacci(50) makes over a TRILLION calls
// Fix with memoization (caching):
Map<Integer, Integer> cache = new HashMap<>();
public int fibMemo(int n) {
    if (n <= 0) return 0;
    if (n == 1) return 1;
    if (cache.containsKey(n)) return cache.get(n);  // already computed? return it
    int result = fibMemo(n - 1) + fibMemo(n - 2);
    cache.put(n, result);  // cache for future use
    return result;
}
// Now fibonacci(50) takes ~50 steps instead of a trillion.
```

Every recursion needs:
1. **Base case** — when to stop (prevents infinite recursion / StackOverflowError)
2. **Recursive case** — break the problem into a smaller version of itself
3. **Progress** — each call must move closer to the base case

---

### Two Pointers Pattern

```java
// ShopEasy promotion: find two products whose prices add up to a gift card value.
// Customer has a $100 gift card. Which two products can they buy?

// BRUTE FORCE — O(n²): check every pair
for (int i = 0; i < prices.length; i++) {
    for (int j = i + 1; j < prices.length; j++) {
        if (prices[i] + prices[j] == 100) return new int[]{i, j};
    }
}

// TWO POINTERS — O(n) on sorted array: one pointer at each end
public int[] findPair(double[] sortedPrices, double target) {
    int left = 0;
    int right = sortedPrices.length - 1;

    while (left < right) {
        double sum = sortedPrices[left] + sortedPrices[right];
        if (sum == target) {
            return new int[]{left, right};  // found the pair
        } else if (sum < target) {
            left++;   // need a bigger sum → move left pointer right
        } else {
            right--;  // need a smaller sum → move right pointer left
        }
    }
    return new int[]{-1, -1};  // no valid pair
}
```

```
Sorted prices: [9.99, 19.99, 29.99, 49.99, 59.99, 79.99, 89.99]
Target gift card: $100

Step 1: left=9.99, right=89.99, sum=99.98 < 100 → move left →
Step 2: left=19.99, right=89.99, sum=109.98 > 100 → move right ←
Step 3: left=19.99, right=79.99, sum=99.98 < 100 → move left →
Step 4: left=29.99, right=79.99, sum=109.98 > 100 → move right ←
Step 5: left=29.99, right=59.99, sum=89.98 < 100 → move left →
Step 6: left=49.99, right=59.99, sum=109.98 > 100 → move right ←
        left >= right → no exact pair found

Only 6 steps instead of 21 (brute force comparisons for 7 items).
```

---

### String Manipulation

```java
// ShopEasy tests frequently validate and manipulate strings.

String productName = "  Wireless Headphones  ";

// Trim whitespace (common in UI text extraction)
productName.trim();               // "Wireless Headphones"
productName.strip();              // "Wireless Headphones" (Java 11+, handles Unicode spaces)

// Case comparison (UI shows "SALE", API returns "sale")
"SALE".equalsIgnoreCase("sale");  // true
"SALE".toLowerCase();             // "sale"

// Check contents
productName.contains("Wireless");     // true
productName.startsWith("Wireless");   // false (leading spaces!)
productName.trim().startsWith("Wireless"); // true

// Split — parse a category breadcrumb
String breadcrumb = "Electronics > Audio > Headphones";
String[] parts = breadcrumb.split(" > ");
// parts = ["Electronics", "Audio", "Headphones"]

// Join — build a comma-separated list for display
List<String> tags = List.of("audio", "wireless", "bluetooth");
String joined = String.join(", ", tags);  // "audio, wireless, bluetooth"

// Replace — sanitize test data
String orderId = "ORD-2024-001";
String numericPart = orderId.replaceAll("[^0-9]", "");  // "2024001"

// StringBuilder — efficient string concatenation in loops
StringBuilder report = new StringBuilder();
for (Product p : products) {
    report.append(p.getName()).append(": $").append(p.getPrice()).append("\n");
}
String result = report.toString();
// "Laptop: $999.99\nHeadphones: $79.99\n..."

// Why StringBuilder? String concatenation with + creates a new String object each time.
// In a loop with 1000 iterations, that's 1000 temporary objects. StringBuilder reuses one buffer.


// INTERVIEW CLASSIC: reverse a string
public String reverse(String s) {
    return new StringBuilder(s).reverse().toString();
}
reverse("ShopEasy");  // "ysaEpohS"

// INTERVIEW CLASSIC: check if a string is a palindrome
public boolean isPalindrome(String s) {
    String clean = s.toLowerCase().replaceAll("[^a-z0-9]", "");
    return clean.equals(new StringBuilder(clean).reverse().toString());
}
isPalindrome("racecar");  // true
isPalindrome("Laptop");   // false


// INTERVIEW CLASSIC: find the first non-repeating character
public char firstUnique(String s) {
    Map<Character, Integer> count = new LinkedHashMap<>();
    for (char c : s.toCharArray()) {
        count.put(c, count.getOrDefault(c, 0) + 1);
    }
    for (Map.Entry<Character, Integer> entry : count.entrySet()) {
        if (entry.getValue() == 1) return entry.getKey();
    }
    return '_';  // no unique character
}
firstUnique("shopeasy");  // 'h' (s appears twice)
```

---

### Summary: Which Data Structure When?

| Need | Data Structure | Why |
|------|---------------|-----|
| Ordered list, access by index | ArrayList | O(1) access, fast iteration |
| Frequent insert/remove at head | LinkedList | O(1) insert/remove at ends |
| Key-value lookup | HashMap | O(1) get/put by key |
| Unique values, no duplicates | HashSet | O(1) add/contains |
| Last-in-first-out (undo, back button) | Stack | push/pop from top |
| First-in-first-out (processing queue) | Queue (LinkedList) | add to back, poll from front |
| Sorted data + fast search | Sorted Array + Binary Search | O(log n) search |
| Count occurrences | HashMap<Item, Integer> | O(1) increment per item |
| Priority processing | PriorityQueue | Smallest/largest always at front |
```

This covers the core DS&A topics that come up in SDET interviews: Big O, arrays, lists, maps, sets, stacks, queues, binary search, sorting, recursion, two pointers, and string manipulation — all grounded in ShopEasy examples.

