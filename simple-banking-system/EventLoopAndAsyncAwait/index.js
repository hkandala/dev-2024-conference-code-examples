class Account {
  constructor(id, balance = 0) {
    this.id = id;
    this.balance = balance;
  }

  async deposit(amount) {
    await this.simulateIOOperation();

    this.balance += amount;
    console.log(`Deposited ${amount} to account ${this.id}`);
  }

  async withdraw(amount) {
    await this.simulateIOOperation();

    if (this.balance >= amount) {
      this.balance -= amount;
      console.log(`Withdrew ${amount} from account ${this.id}`);
    }
  }

  getAccountID() {
    return this.id;
  }

  getBalance() {
    return this.balance;
  }

  async simulateIOOperation() {
    const min = 100;
    const max = 500;
    const timeout = Math.floor(Math.random() * (max - min + 1)) + min;
    await new Promise((resolve) => setTimeout(resolve, timeout));
  }
}

async function main() {
  let accounts = [];
  for (let i = 0; i < 5; i++) {
    accounts.push(new Account(i, 100));
  }

  let operations = [];
  for (let i = 0; i < 1000; i++) {
    const idx = Math.floor(Math.random() * 5);
    const amount = Math.floor(Math.random() * 91) + 10;

    operations.push(
      (async () => {
        if (Math.random() > 0.5) {
          await accounts[idx].deposit(amount);
        } else {
          await accounts[idx].withdraw(amount);
        }
      })(),
    );
  }

  await Promise.all(operations);

  console.log("\nFinal Balances:");
  accounts.forEach((account) => {
    console.log(`Account ${account.getAccountID()}: ${account.getBalance()}`);
  });
}

main();
