package uk.gov.dwp.uc.pairtest;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


@Slf4j
public class TicketServiceImpl implements TicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketServiceImpl.class);

    private int totalNumberOfTickets;
    private int totalSeatsToAllocate;
    private int totalNumberOfInfant;
    private int totalNumberOfAdult;
    private int totalNumberOfChild;
    private int totalPayment;


    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {
        if(Math.toIntExact(accountId)<=0){
            getException("The accountId is not greater than zero and it is invalid.");
        }
        ticketCalculator(ticketTypeRequests);
        validateTickets();
        ticketPayment(accountId);
        seatReservation(accountId);
        LOGGER.info("{} tickets have been booked for "+ "{} adult/adults, {} child/children, and "+
                        "{} infant/infants. ", totalNumberOfTickets, totalNumberOfAdult, totalNumberOfChild,
                totalNumberOfInfant );
    }

    private void ticketPayment(Long accountId) {
        TicketPaymentServiceImpl ticketPaymentServiceImpl = new TicketPaymentServiceImpl();
        ticketPaymentServiceImpl.makePayment(accountId, totalPayment);
        LOGGER.info("Total amount of £{} has been paid.", totalPayment);
    }


    private void seatReservation(long accountId) {
        SeatReservationService seatReservationService = new SeatReservationService()
        {
            @Override
            public void reserveSeat(long accountId, int totalSeatsToAllocate) {
                //The seat reservation code
            }
        };
        seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
        LOGGER.info("{} seats are reserved.", totalSeatsToAllocate);
    }

    private void ticketCalculator(TicketTypeRequest... ticketTypeRequests) {
        int totalTickets=0;
        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            TicketTypeRequest.Type ticketType = ticketTypeRequest.getTicketType();
            totalNumberOfTickets = ticketTypeRequest.getNoOfTickets();

            int numberOfAdult=0;
            int numberOfChild=0;
            int numberOfInfant=0;

            if(Price.ADULT_PRICE.getKey().equalsIgnoreCase(ticketType.toString())){
                numberOfAdult= getTotalNumberOfTicketType(numberOfAdult, Price.ADULT_PRICE.getValue());

            } else if(Price.CHILD_PRICE.getKey().equalsIgnoreCase(ticketType.toString())){
                numberOfChild= getTotalNumberOfTicketType(numberOfChild, Price.CHILD_PRICE.getValue());

            }else if(Price.INFANT_PRICE.getKey().equalsIgnoreCase(ticketType.toString())){
                numberOfInfant = getTotalNumberOfTicketType(numberOfInfant, Price.INFANT_PRICE.getValue());
            }

            totalTickets += numberOfChild+numberOfAdult+numberOfInfant;
            totalSeatsToAllocate+= (numberOfChild+numberOfAdult);
            totalNumberOfAdult+=numberOfAdult;
            totalNumberOfInfant+=numberOfInfant;
            totalNumberOfChild+=numberOfChild;
        }
        totalNumberOfTickets = totalTickets;
    }

    private int getTotalNumberOfTicketType(int totalNumberOfTicketType, int price){
        totalPayment += price>0 ?  (price * totalNumberOfTickets) : price;
        totalNumberOfTicketType += totalNumberOfTickets;
        totalNumberOfTickets += totalNumberOfTickets;

        return totalNumberOfTicketType;
    }

    private void validateTickets(){
        if(totalNumberOfTickets >20){
            getException("Only a maximum of 20 tickets that can be purchased at a time.");
        }
        if(totalNumberOfAdult <= 0){
            getException("Child and Infant tickets cannot be purchased without " +
                    "purchasing an Adult ticket.");
        }
        if(totalNumberOfInfant>0 && totalNumberOfInfant> totalNumberOfAdult){
            getException("Total number of Infants is greater than adult.");
        }
    }

    private void getException(String errorMessage){
        LOGGER.error(errorMessage);
        throw new InvalidPurchaseException();
    }

    private enum Price {
        ADULT_PRICE("Adult", 20),
        CHILD_PRICE("Child", 10),
        INFANT_PRICE("Infant", 0);

        private final String key;
        private final int value;

        Price(String key, int value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }
        public int getValue() {
            return value;
        }
    }
}
