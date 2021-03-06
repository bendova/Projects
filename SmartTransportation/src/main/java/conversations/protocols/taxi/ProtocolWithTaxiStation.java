package conversations.protocols.taxi;

import java.util.UUID;

import conversations.commonActions.SendMessageAction;
import conversations.taxiStationTaxi.ConversationDescription;
import conversations.taxiStationTaxi.actions.*;
import conversations.taxiStationTaxi.messages.RegisterAsTaxiMessage;
import conversations.taxiStationTaxi.messages.RejectOrderMessage;
import conversations.taxiStationTaxi.messages.RevisionCompleteMessage;
import conversations.taxiStationTaxi.messages.TaxiOrderCompleteMessage;
import conversations.taxiStationTaxi.messages.TaxiOrderMessage;
import conversations.taxiStationTaxi.messages.TaxiStatusUpdateMessage;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.UnicastMessage;
import uk.ac.imperial.presage2.util.protocols.Conversation;
import util.protocols.*;

public class ProtocolWithTaxiStation 
{
	private NetworkAdaptor mAdaptor;
	private FSMProtocol mWithTaxiStationProtocol;
	private UUID mConversationKey;
	
	public ProtocolWithTaxiStation(NetworkAdaptor adaptor)
	{
		assert(adaptor != null);
		
		mAdaptor = adaptor;
	}
	
	public void init(OnReceiveOrderAction onReceiveOrderAction)
	{
		assert(mWithTaxiStationProtocol == null);
		
		ConversationDescription description = new ConversationDescription();
		SendMessageAction onRegisterAction = new SendMessageAction(); 
		SendMessageAction onOrderCompleteAction = new SendMessageAction(); 
		SendMessageAction onRejectOrderAction = new SendMessageAction(); 
		SendMessageAction onGoingToRevisionAction = new SendMessageAction();
		SendMessageAction onRevisionCompleteAction = new SendMessageAction();
		description.init(onRegisterAction, onReceiveOrderAction, onOrderCompleteAction, 
				onRejectOrderAction, onGoingToRevisionAction, onRevisionCompleteAction);
		
		mWithTaxiStationProtocol = new FSMProtocol(ConversationDescription.PROTOCOL_NAME, description, mAdaptor);
	}
	
	public void registerAsTaxi(RegisterAsTaxiMessage msg)
	{
		Conversation conversation = mWithTaxiStationProtocol.spawn(msg.getTo());
		mConversationKey = conversation.getID();
		sendMessage(msg);
	}
	
	public void handleOrderMessage(TaxiOrderMessage msg)
	{
		mWithTaxiStationProtocol.handle(msg);
	}
	
	public void reportOrderComplete(TaxiOrderCompleteMessage msg)
	{
		sendMessage(msg);
	}
	
	public void rejectOrder(RejectOrderMessage msg)
	{
		sendMessage(msg);
	}
	
	public void sendStatusUpdate(TaxiStatusUpdateMessage msg)
	{
		sendMessage(msg);
	}
	
	public void handleRevisionCompleteMessage(RevisionCompleteMessage msg)
	{
		mWithTaxiStationProtocol.handle(msg);
	}
	
	private void sendMessage(UnicastMessage<?> msg)
	{
		msg.setProtocol(ConversationDescription.PROTOCOL_NAME);
		msg.setConversationKey(mConversationKey);
		mWithTaxiStationProtocol.handle(msg);
	}
}
