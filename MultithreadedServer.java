package hw09;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// TO DO: Task is currently an ordinary class.
// You will need to modify it to make it a task,
// so it can be given to an Executor thread pool.
//
class Task implements Runnable {
	private static final int A = constants.A;
	private static final int Z = constants.Z;
	private static final int numLetters = constants.numLetters;

	int[] orgVals = new int[numLetters];
	int[] cacheVals = new int[numLetters];
	boolean[] written = new boolean[numLetters];
	boolean[] read = new boolean[numLetters];
	boolean[] originals = new boolean[numLetters];
	boolean[] openAccts = new boolean[numLetters];

	private Account[] accounts;
	private String transaction;

	// TO DO: The sequential version of Task peeks at accounts
	// whenever it needs to get a value, and opens, updates, and closes
	// an account whenever it needs to set a value. This won't work in
	// the parallel version. Instead, you'll need to cache values
	// you've read and written, and then, after figuring out everything
	// you want to do, (1) open all accounts you need, for reading,
	// writing, or both, (2) verify all previously peeked-at values,
	// (3) perform all updates, and (4) close all opened accounts.

	public Task(Account[] allAccounts, String trans) {
		accounts = allAccounts;
		transaction = trans;
	}

	// TO DO: parseAccount currently returns a reference to an account.
	// You probably want to change it to return a reference to an
	// account *cache* instead.
	//
	private int parseAccount(String name, boolean lhs) {
		int rtn;
		int accountNum = (int) (name.charAt(0)) - (int) 'A';
		if (accountNum < A || accountNum > Z)
			throw new InvalidTransactionError();

		if (originals[accountNum] == false) {
			originals[accountNum] = true;
			read[accountNum] = true;
			orgVals[accountNum] = accounts[accountNum].peek();
			cacheVals[accountNum] = orgVals[accountNum];
		}
		if (lhs == true){
			read[accountNum] = false;
			written[accountNum] = true;
		}
		rtn = accountNum;
		
		for (int i = 1; i < name.length(); i++) {
			if (name.charAt(i) != '*')
				throw new InvalidTransactionError();
			// all accounts peeked at must be opened for reading
			accountNum = (accounts[accountNum].peek() % numLetters);
			if (originals[accountNum] == false) {
				read[accountNum] = true;
				originals[accountNum] = true;
				orgVals[accountNum] = accounts[accountNum].peek();
				cacheVals[accountNum] = orgVals[accountNum];
			}
			rtn = accountNum;
		}
		return rtn;
	}

	private int parseAccountOrNum(String name) {
		int rtn;
		if (name.charAt(0) >= '0' && name.charAt(0) <= '9') {
			rtn = new Integer(name).intValue();
		} else {
			rtn = cacheVals[parseAccount(name, false)];
			// original val
		}
		return rtn;
	}

	public void run() {
		// tokenize transaction
		String[] commands = transaction.split(";");
		int i;
		while (true) {
			// for the duration of command
			for (i = 0; i < commands.length; i++) {
				String[] words = commands[i].trim().split("\\s");
				// if less than 3 then invalid
				if (words.length < 3)
					throw new InvalidTransactionError();
				// lhs set equal to the account
				int lhs = parseAccount(words[0], true);
				
				// catch
				if (!words[1].equals("="))
					throw new InvalidTransactionError();
				// rhs set equal to int value
				int rhs = parseAccountOrNum(words[2]);
				for (int j = 3; j < words.length; j += 2) {
					if (words[j].equals("+"))
						rhs += parseAccountOrNum(words[j + 1]);
					else if (words[j].equals("-"))
						rhs -= parseAccountOrNum(words[j + 1]);
					else
						throw new InvalidTransactionError();
				}
				// rhs = sum of transaction
				cacheVals[lhs] = rhs;
				//
			}// close of parse command loop
// ========================================================================================

			try {
				// phase 1 of 2
				Arrays.fill(openAccts, false);
				for (i = A; i <= Z; i++) {
					// true means write
					// false means read
					// if account is already open to writing
					if (written[i] == true){
						accounts[i].open(true);
						openAccts[i] = true;
					}

					// if account is already open to reading
					if (read[i] == true){
						accounts[i].open(false);
						openAccts[i] = true;
					}
				}// close open accounts loop
			} catch (TransactionAbortException e) {
				for (int j = Z; j >= A; j--) {
					if (openAccts[j] == true)
						accounts[j].close();

				}
				// clear vals
				Arrays.fill(originals, false);
				Arrays.fill(openAccts, false);
				continue;
			}

// ============================================================================================

			try {
				Arrays.fill(openAccts, false);
				// verify for loop
				for (i = A; i <= Z; i++) {
					if (read[i] == true)
						accounts[i].verify(orgVals[i]);
				}// close verify loop
			} catch (TransactionAbortException e) {
				for (int j = Z; j >= A; j--) {
					if (read[j] == true || written[j] == true)
						accounts[j].close();

				}
				Arrays.fill(originals, false);
				continue;
			}

// ================================================================================================
			// update for loop
			for (i = A; i <= Z; i++) {
				if (written[i] == true) {
					accounts[i].update(cacheVals[i]);
					accounts[i].close();
				}
				if (read[i] == true) {
					accounts[i].close();
				}

			}
			break;

		}
		System.out.println("commit: " + transaction);
	}
}

public class MultithreadedServer {

	// requires: accounts != null && accounts[i] != null (i.e., accounts are
	// properly initialized)
	// modifies: accounts
	// effects: accounts change according to transactions in inputFile
	public static void runServer(String inputFile, Account accounts[])
			throws IOException {

		// read transactions from input file
		String line;
		BufferedReader input = new BufferedReader(new FileReader(inputFile));

		// TO DO: you will need to create an Executor and then modify the
		// following loop to feed tasks to the executor instead of running them
		// directly.
		ExecutorService pool = Executors.newCachedThreadPool();

		while ((line = input.readLine()) != null) {
			Task t = new Task(accounts, line);
			pool.execute(t);
		}
		pool.shutdown();
		try {
			pool.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		input.close();

	}
}
