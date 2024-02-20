package main

import (
	"fmt"
	"math/rand"
	"sync"
)

type Account struct {
	id           int
	balance      int
	transactions chan int
}

func NewAccount(id int, initialBalance int) *Account {
	acc := &Account{
		id:           id,
		balance:      initialBalance,
		transactions: make(chan int),
	}
	go acc.processTransactions()
	return acc
}

func (acc *Account) processTransactions() {
	for transaction := range acc.transactions {
		if transaction > 0 {
			acc.balance += transaction
			fmt.Printf("Deposited %d to account %d\n", transaction, acc.id)
		} else {
			if acc.balance+transaction >= 0 {
				acc.balance += transaction
				fmt.Printf("Withdrew %d from account %d\n", -transaction, acc.id)
			}
		}
	}
}

func (acc *Account) Deposit(amount int) {
	acc.transactions <- amount
}

func (acc *Account) Withdraw(amount int) {
	acc.transactions <- -amount
}

func (acc *Account) GetAccountID() int {
	return acc.id
}

func (acc *Account) GetBalance() int {
	return acc.balance
}

func main() {
	var wg sync.WaitGroup

	accounts := make([]*Account, 5)
	for i := range accounts {
		accounts[i] = NewAccount(i, 100)
	}

	for i := 0; i < 1000; i++ {
		idx := rand.Intn(5)
		amount := rand.Intn(91) + 10
		wg.Add(1)

		go func(idx, amount int) {
			defer wg.Done()
			if rand.Float64() > 0.5 {
				accounts[idx].Deposit(amount)
			} else {
				accounts[idx].Withdraw(amount)
			}
		}(idx, amount)
	}

	wg.Wait()

	fmt.Printf("\nFinal Balances:\n")
	for i := range accounts {
		fmt.Printf("Account %d: %d\n", accounts[i].GetAccountID(), accounts[i].GetBalance())
	}
}
