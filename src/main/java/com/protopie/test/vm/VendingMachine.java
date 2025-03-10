package com.protopie.test.vm;

import com.protopie.test.config.Payment;
import com.protopie.test.config.StringConfig;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VendingMachine extends Thread {

    private final Map<String, Integer> stock = new LinkedHashMap<>();
    private final String[] availDrink = { "coke", "water", "coffee" };
    private final int[] drinkPrice = { 1100, 600, 700 };
    private final String[] paymentType = { "CREDIT_CARD", "DEBIT_CARD", "CASH" };
    private final Integer[] cashType = { 100, 500, 1000, 5000, 10000 };

    private final AtomicInteger totalCash = new AtomicInteger(0);

    private final Scanner scanner = new Scanner(System.in);
    private final StringBuilder sb = new StringBuilder();

    @Override
    public void run() {
        generateSettings();
        printStockStatus(StringConfig.MESSAGE_ASK_DRINK);
        choosePayment();
    }

    private void generateSettings() {
        int totalDrinkCount = (int) (Math.random() * 10) + 11;
        int cokeCount = 1;
        int waterCount = 1;
        int coffeeCount = 1;
        for(int i=0; i<totalDrinkCount; i++) {
            int drinkType = (int) (Math.random() * 3);
            String drinkName = availDrink[drinkType];
            int drinkCount = (int) (Math.random() * 10) + 1;
            if(drinkName.equals(availDrink[0])) {
                stock.put(drinkName + "_" + cokeCount, drinkCount);
                cokeCount++;
            } else if(drinkName.equals(availDrink[1])) {
                stock.put(drinkName + "_" + waterCount, drinkCount);
                waterCount++;
            } else if(drinkName.equals(availDrink[2])) {
                stock.put(drinkName + "_" + coffeeCount, drinkCount);
                coffeeCount++;
            }
        }
    }

    private void printStockStatus(String comment) {
        System.out.println(StringConfig.MESSAGE_DRINK_STOCK);
        int count = 1;
        for(String key : stock.keySet()) {
            if(stock.get(key) > 0) {
                String type = key.split("_")[0];
                sb.append("[").append(count).append("] ").append(key).append(" : ");
                if(type.equals(availDrink[0])) {
                    sb.append(drinkPrice[0]);
                } else if(type.equals(availDrink[1])) {
                    sb.append(drinkPrice[1]);
                } else if(type.equals(availDrink[2])) {
                    sb.append(drinkPrice[2]);
                }
                count++;
                sb.append("원 (").append(stock.get(key)).append("개)");
                if(stock.size() >= count) sb.append(System.lineSeparator());
            }
        }
        System.out.println(sb);
        sb.setLength(0);
        System.out.print(comment);
    }

    private void choosePayment() {
        for(int i=0; i<paymentType.length; i++) {
            sb.append("[").append((i+1)).append("] ").append(paymentType[i]);
            if(paymentType.length - 1 > i) sb.append(System.lineSeparator());
        }
        System.out.println(sb);
        sb.setLength(0);
        System.out.print(StringConfig.MESSAGE_CHOOSE_PAYMENT);
        int paymentId = Integer.parseInt(scanner.nextLine()) - 1;
        switch(paymentId) {
            case 0:
                payWithCreditCard();
                break;
            case 1:
                payWithDebitCard();
                break;
            case 2:
                payWithCash();
                break;
        }
    }

    private void payWithCash() {
        System.out.print(StringConfig.MESSAGE_PAY_WITH_CASH);
        String input = scanner.nextLine();
        int type = Integer.parseInt(input.split(" ")[0]);
        int count = Integer.parseInt(input.split(" ")[1]);
        if(!Arrays.asList(cashType).contains(type)) throw new IllegalArgumentException(StringConfig.ERROR_MESSAGE_INVALID_CASH_TYPE);
        int total = totalCash.addAndGet(type * count);
        System.out.println("투입 금액: " + total + "원");
        selectDrink(total, Payment.CASH.name());
    }

    private void payWithCreditCard() {
        System.out.print(StringConfig.MESSAGE_SET_VALID_DATE);
        String date = scanner.nextLine();
        String year = date.split("/")[1];
        String month = date.split("/")[0];
        if(month.startsWith("0")) month = month.substring(1);
        LocalDate validDate = LocalDate.of(
                Integer.parseInt("20"+year),
                Integer.parseInt(month),
                1);
        LocalDate now = LocalDate.now();
        if(now.isAfter(validDate)) {
            System.out.println(StringConfig.MESSAGE_AFTER_VALID_DATE);
            System.out.print(StringConfig.MESSAGE_HAVE_ANOTHER_PAYMENT);
            String input = scanner.nextLine();
            if(input.equals("Y")) {
                choosePayment();
            } else {
                end();
            }
        } else {
            System.out.print(StringConfig.MESSAGE_SET_PAYMENT_LIMIT);
            int limit = Integer.parseInt(scanner.nextLine());
            selectDrink(limit, Payment.CARD.name());
        }
    }

    private void payWithDebitCard() {
        System.out.print(StringConfig.MESSAGE_SET_DEBIT_LIMIT);
        int balance = Integer.parseInt(scanner.nextLine());
        selectDrink(balance, Payment.CARD.name());
    }

    private void selectDrink(int money, String payment) {
        printStockStatus(StringConfig.MESSAGE_SELECT_DRINK);
        String target = scanner.nextLine();
        if(!target.contains("_")) throw new IllegalArgumentException(StringConfig.ERROR_MESSAGE_INVALID_DRINK);
        if(!stock.containsKey(target)) throw new IllegalArgumentException(StringConfig.ERROR_MESSAGE_INVALID_DRINK);
        String type = target.split("_")[0];
        int price = 0;
        if(type.equals(availDrink[0])) {
            price = drinkPrice[0];
        } else if(type.equals(availDrink[1])) {
            price = drinkPrice[1];
        } else if(type.equals(availDrink[2])) {
            price = drinkPrice[2];
        }
        if(money > price) {
            System.out.println("음료수(" + target + ")가 나온다.");
            System.out.println("음료수(" + target + ")를 꺼낸다.");
            int stockCount = stock.get(target) - 1;
            if(stockCount > 0) stock.put(target, stockCount);
            else stock.remove(target);
            System.out.print(StringConfig.MESSAGE_ASK_ANOTHER_DRINK);
            String input = scanner.nextLine();
            if(input.equals("Y")) {
                if(payment.equals(Payment.CASH.name())) {
                    int remain = money - price;
                    totalCash.set(remain);
                    selectDrink(totalCash.get(), Payment.CASH.name());
                } else {
                    choosePayment();
                }
            } else {
                if(payment.equals(Payment.CASH.name())) {
                    int remain = money - price;
                    if(remain > 0) {
                        System.out.println("거스름돈("+remain+"원)이 나온다.");
                        System.out.println("거스름돈("+remain+"원)을 꺼낸다.");
                    }
                }
                end();
            }
        } else {
            System.out.println(StringConfig.MESSAGE_NOT_ENOUGH_MONEY);
            if(payment.equals(Payment.CASH.name())) {
                payWithCash();
            } else {
                choosePayment();
            }
        }
    }

    private void end() {
        System.out.println(StringConfig.MESSAGE_END);
    }
}
