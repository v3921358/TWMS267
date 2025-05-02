package Database.mapper;

import Server.auction.AuctionItem;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuctionItemMapper implements IMapper<AuctionItem> {

    @Override
    public AuctionItem mapper(ResultSet rs) throws SQLException {
        AuctionItem auctionItem = new AuctionItem();
        auctionItem.id = rs.getLong("id");
        auctionItem.accounts_id = rs.getInt("accounts_id");
        auctionItem.characters_id = rs.getInt("characters_id");
        auctionItem.owner = rs.getString("owner");
        auctionItem.other_id = rs.getInt("other_id");
        auctionItem.other = rs.getString("other");
        auctionItem.state = rs.getInt("state");
        auctionItem.type = rs.getInt("type");
        auctionItem.itemid = rs.getInt("itemid");
        auctionItem.price = rs.getLong("price");
        auctionItem.startdate = rs.getLong("startdate");
        auctionItem.expiredate = rs.getLong("expiredate");
        auctionItem.donedate = rs.getLong("donedate");
        auctionItem.number = rs.getInt("number");
        return auctionItem;
    }
}
