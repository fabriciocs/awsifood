import customer from 'app/entities/customer/customer.reducer';
import restaurant from 'app/entities/restaurant/restaurant.reducer';
import menu from 'app/entities/menu/menu.reducer';
import dish from 'app/entities/dish/dish.reducer';
import order from 'app/entities/order/order.reducer';
import orderItem from 'app/entities/order-item/order-item.reducer';
import payment from 'app/entities/payment/payment.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  customer,
  restaurant,
  menu,
  dish,
  order,
  orderItem,
  payment,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
