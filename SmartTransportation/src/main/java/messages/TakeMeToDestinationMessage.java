package messages;

import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.network.UnicastMessage;
import uk.ac.imperial.presage2.util.location.Location;
import util.TimeStamp;

public class TakeMeToDestinationMessage extends UnicastMessage<Location>
{
	public TakeMeToDestinationMessage(Location targetLocation, NetworkAddress from, NetworkAddress to)
	{
		super(Performative.INFORM, from, to, new TimeStamp());
		
		assert(targetLocation != null);
		data = targetLocation;
	}
}
