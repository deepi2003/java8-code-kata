package stream.api;

import common.test.tool.annotation.Difficult;
import common.test.tool.dataset.ClassicOnlineStore;
import common.test.tool.entity.Customer;
import common.test.tool.entity.Item;
import common.test.tool.entity.Shop;

import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class Exercise8Test extends ClassicOnlineStore {

    @Difficult @Test
    public void itemsNotOnSale() {
        Stream<Customer> customerStream = this.mall.getCustomerList().stream();
        Stream<Shop> shopStream = this.mall.getShopList().stream();

        /**
         * Create a set of item names that are in {@link Customer.wantToBuy} but not on sale in any shop.
         */
        List<String> itemOnSale =  shopStream.flatMap(shop -> shop.getItemList().stream())
                .map(item ->item.getName()).collect(toList());

        Set<String> itemSetNotOnSale = customerStream.flatMap(customer -> customer.getWantToBuy().stream())
                .map(item -> item.getName())
                .filter( item -> !itemOnSale.contains(item))
                .collect(Collectors.toSet());

        assertThat(itemSetNotOnSale, hasSize(3));
        assertThat(itemSetNotOnSale, hasItems("bag", "pants", "coat"));
    }

    @Difficult @Test
    public void havingEnoughMoney() {
        Stream<Customer> customerStream = this.mall.getCustomerList().parallelStream();
        Stream<Shop> shopStream = this.mall.getShopList().stream();

        /**
         * Create a customer's name list including who are having enough money to buy all items they want which is on sale.
         * Items that are not on sale can be counted as 0 money cost.
         * If there is several same items with different prices, customer can choose the cheapest one.
         */


        List<Item> itemOnSale = shopStream.flatMap(shop ->shop.getItemList().stream())
                .distinct()
                .collect(Collectors.toList());

        List<String> customerNameList = customerStream.filter(customer ->
                customer.getBudget() >= getCustomerWishListPrice(customer, itemOnSale))
                .map(Customer::getName)
                .collect(Collectors.toList());

        assertThat(customerNameList, hasSize(7));
        assertThat(customerNameList, hasItems("Joe", "Patrick", "Chris", "Kathy", "Alice", "Andrew", "Amy"));
    }

    private Integer getCustomerWishListPrice(Customer customer, List<Item> itemOnSale) {
        return customer.getWantToBuy().stream()
                .mapToInt( item ->getItemPrice(item, itemOnSale))
                .sum();
    }

    private int getItemPrice(Item item, List<Item> itemOnSale) {
        return itemOnSale.parallelStream()
                .filter(item1 -> item1.getName().equalsIgnoreCase(item.getName()))
                .sorted(Comparator.comparing(Item::getPrice).reversed())
                .findAny()
                .map(Item::getPrice)
                .orElse(0);

    }

}
