package hw09.test;

import hw09.*;

import java.io.*;
import java.lang.Thread.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

public class MultithreadedServerTests extends TestCase {
	private static final int A = constants.A;
	private static final int Z = constants.Z;
	private static final int numLetters = constants.numLetters;
	private static Account[] accounts;

	protected static void dumpAccounts() {
		// output values:
		for (int i = A; i <= Z; i++) {
			System.out.print("    ");
			if (i < 10)
				System.out.print("0");
			System.out.print(i + " ");
			System.out.print(new Character((char) (i + 'A')) + ": ");
			accounts[i].print();
			System.out.print(" (");
			accounts[i].printMod();
			System.out.print(")\n");
		}
	}

	@Test
	public void testIncrement() throws IOException {

		// initialize accounts
		accounts = new Account[numLetters];
		for (int i = A; i <= Z; i++) {
			accounts[i] = new Account(Z - i);
		}

		MultithreadedServer.runServer("hw09/data/increment", accounts);

		// assert correct account values
		for (int i = A; i <= Z; i++) {
			Character c = new Character((char) (i + 'A'));
			assertEquals("Account " + c + " differs", Z - i + 1,
					accounts[i].getValue());
		}

	}

	@Test
	public void test02() throws IOException {

		// initialize accounts
		accounts = new Account[numLetters];
		for (int i = A; i <= Z; i++) {
			accounts[i] = new Account(Z - i);
		}

		MultithreadedServer.runServer("hw09/data/test02", accounts);

		// assert correct account values
		assertEquals("Account A differs", 25, accounts[0].getValue());
		assertEquals("Account B differs", 51, accounts[1].getValue());
		assertEquals("Account C differs", 26, accounts[2].getValue());

	}

	@Test
	public void test03() throws IOException {

		// initialize accounts
		accounts = new Account[numLetters];
		accounts[0] = new Account(0);
		accounts[1] = new Account(0);
		accounts[5] = new Account(9);
		accounts[9] = new Account(5);
		accounts[11] = new Account(5);
		accounts[16] = new Account(37);

		MultithreadedServer.runServer("hw09/data/test03", accounts);

		// assert correct account values
		assertEquals("Account A differs", 9, accounts[0].getValue());
		assertEquals("Account B differs", 14, accounts[1].getValue());
		assertEquals("Account F differs", 9, accounts[5].getValue());
		assertEquals("Account J differs", 5, accounts[9].getValue());

	}

	@Test
	public void test05() throws IOException {

		// initialize accounts
		accounts = new Account[numLetters];
		for (int i = A; i <= Z; i++) {
			accounts[i] = new Account(Z - i);
		}

		MultithreadedServer.runServer("hw09/data/test05", accounts);

	}

}