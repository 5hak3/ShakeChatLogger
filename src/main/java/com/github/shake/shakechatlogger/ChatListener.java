package com.github.shake.shakechatlogger;

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelChatEvent;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ChatListener implements Listener {
    private final Config config;

    public ChatListener(Config config) {
        this.config = config;
    }

    /**
     * Global/Channelチャット監視
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // LCが入っていない時の処理
        if (!this.config.isEnabledLC) {
            this.gChat(player, message);
            return;
        }

        // LCが入っている時の処理
        Channel channel = this.config.lunaChatAPI.getDefaultChannel(player.getName());
        // globalのとき
        if (channel == null || message.startsWith("!") || message.startsWith("#!")) {
            this.gChat(player, message);
        }
        // globalじゃない時 -> public void onChat(LunaChatBukkitChannelChatEvent event)へ
    }

    @EventHandler
    public void onChat(LunaChatBukkitChannelChatEvent event) {
        ChannelMember chMember = event.getMember();
        Channel ch = event.getChannel();
        Player player = Bukkit.getPlayer(chMember.getName());
        String message = event.getPreReplaceMessage();
        if (ch.isPersonalChat()) {
            ArrayList<ChannelMember> members = new ArrayList<>(ch.getMembers());
            members.remove(chMember);
            Player toPlayer = Bukkit.getPlayer(members.get(0).getName());
            pChat(player, message, toPlayer);
        }
        else {
            cChat(player, message, ch.getName());
        }
    }

    /**
     * PrivateチャットをCommandPreprocessでキャッチする
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (this.config.isEnabledLC) return;

        String raw = event.getMessage();
        ArrayList<String> stList = new ArrayList<>(Arrays.asList(raw.split(" ")));
        // stListの要素数が1以下だと引数がないので少なくとも対象コマンドではない
        if (stList.size() < 2) return;

        String command = stList.get(0);
        Player toPlayer = null;
        switch (command.toLowerCase(Locale.ROOT)) {
            // r以外
            case "/msg":
            case "/tell":
            case "/w":
            case "/tellraw":
                toPlayer = Bukkit.getPlayer(stList.get(1));
                stList.remove(0);

            // rは返信先がAPIでは取得できないのでnullとする
            case "/r":
                // この時点でstListの要素数が1以下だとメッセージがないのでエラー
                if (stList.size() < 2) return;
                Player player = event.getPlayer();
                stList.remove(0);
                String message = String.join(" ", stList);
                pChat(player, message, toPlayer);

            default:
                break;
        }
    }

    /**
     * バニラ/LC共通のGlobalチャット用
     * @param player 送信プレイヤー
     * @param message 送信メッセージ
     */
    private void gChat(Player player, String message) {
        // TODO なぜかDBに反映されない
        try (Connection conn = this.config.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO chat_global(dtime, mcid, uuid, nick, content) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?)")) {
            setStatements(player, message, stmt);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * LCのChannelチャット用
     * @param player 送信プレイヤー
     * @param message 送信メッセージ
     * @param channel 送信先チャンネル
     */
    private void cChat(Player player, String message, String channel) {
        try (Connection conn = this.config.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO chat_channel(dtime, mcid, uuid, nick, content, ch) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)")) {
            setStatements(player, message, stmt);
            stmt.setString(5, channel);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * バニラ/LC共通のPrivateチャット用
     */
    private void pChat(Player player, String message, Player toPlayer) {
        try (Connection conn = this.config.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO chat_private(dtime, mcid, uuid, nick, content, to_mcid, to_uuid, to_nick) " +
                        "VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)")) {
            setStatements(player, message, stmt);
            if (toPlayer != null) {
                stmt.setString(5, toPlayer.getName());
                stmt.setString(6, toPlayer.getUniqueId().toString());
                if (this.config.isEnabledESS) {
                    stmt.setString(7, this.config.ess.getUser(toPlayer).getNick());
                }
                else {
                    stmt.setNull(7, Types.VARCHAR);
                }
            }
            else {
                stmt.setNull(5, Types.VARCHAR);
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.VARCHAR);
            }
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 共通するステートメントをstmtにセットする
     * @param player 送信プレイヤー
     * @param message 送信メッセージ
     * @param stmt セット先のPreparedStatement
     * @throws SQLException ステートメントのセットに失敗した時
     */
    private void setStatements(Player player, String message, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, player.getName());
        stmt.setString(2, player.getUniqueId().toString());
        if (this.config.isEnabledESS) {
            stmt.setString(3, this.config.ess.getUser(player).getNick());
        }
        else {
            stmt.setNull(3, Types.VARCHAR);
        }
        stmt.setString(4, message);
    }
}
