"use client";

import { useState } from "react";
import { ArrowLeft, Gamepad2, Heart, RotateCcw, Sparkles, Trophy, UsersRound, X } from "lucide-react";
import { games } from "../lib/demo-data";

function getWinner(board) {
  const lines = [[0, 1, 2], [3, 4, 5], [6, 7, 8], [0, 3, 6], [1, 4, 7], [2, 5, 8], [0, 4, 8], [2, 4, 6]];
  for (const [a, b, c] of lines) if (board[a] && board[a] === board[b] && board[a] === board[c]) return board[a];
  return board.every(Boolean) ? "draw" : null;
}

export default function ArcadeView({ coins, onReward }) {
  const [category, setCategory] = useState("All games");
  const [activeGame, setActiveGame] = useState(null);
  const [board, setBoard] = useState(Array(9).fill(null));
  const [totalWins, setTotalWins] = useState(18);
  const [claimed, setClaimed] = useState(false);
  const categories = ["All games", "Quick matches", "Arcade classics", "Party games"];
  const filteredGames = games.filter((game) => category === "All games" || (category === "Quick matches" ? game.id === "tic-tac-toe" : category === "Arcade classics" ? ["space-shooter", "brick-breaker"].includes(game.id) : game.id === "trivia"));
  const winner = getWinner(board);

  function openGame(game) {
    setActiveGame(game);
    setBoard(Array(9).fill(null));
    setClaimed(false);
  }

  function playCell(index) {
    if (board[index] || winner) return;
    const next = [...board];
    next[index] = next.filter(Boolean).length % 2 === 0 ? "X" : "O";
    setBoard(next);
  }

  function finishRound() {
    if (claimed) return;
    setClaimed(true);
    onReward(25);
    setTotalWins((wins) => wins + 1);
  }

  return (
    <section className="arcade-page">
      <div className="section-heading arcade-heading"><div><p className="eyebrow">READY PLAYER ONE</p><h1>The arcade</h1><p className="section-subtitle">Pick a game. Find your people. Make it a story.</p></div><button className="coin-pill" type="button"><span>✦</span> {coins.toLocaleString()} <small>COINS</small></button></div>
      <div className="arcade-stats-row"><div className="arcade-stat"><span className="stat-icon mint-icon"><Trophy size={17} /></span><span><small>YOUR WINS</small><strong>{totalWins}</strong></span><i>+3 this week</i></div><div className="arcade-stat"><span className="stat-icon violet-icon"><UsersRound size={17} /></span><span><small>PLAYING NOW</small><strong>4,892</strong></span><i className="green-text">● Live</i></div><div className="arcade-stat streak-stat"><span className="stat-icon orange-icon">✦</span><span><small>DAILY STREAK</small><strong>5 days</strong></span><button type="button" onClick={(event) => { event.currentTarget.textContent = "Checked in"; }}>Check in</button></div></div>
      <div className="arcade-featured" style={{ backgroundImage: "linear-gradient(90deg, rgba(10,11,18,.98) 0%, rgba(10,11,18,.78) 44%, rgba(10,11,18,.05) 100%), url('/images/space-shooter.jpg')" }}><div className="featured-copy"><span className="featured-kicker"><Sparkles size={13} /> FEATURED THIS WEEK</span><h2>One more run.<br /><em>One more high score.</em></h2><p>Space Shooter is taking over the leaderboard. Think you can top it?</p><button type="button" onClick={() => openGame(games[0])}>Play Space Shooter <span>↗</span></button></div><span className="feature-rank"><Trophy size={14} /> WEEKLY CHALLENGE</span></div>
      <div className="arcade-section-heading"><div><h2>Find your next game</h2><p>Quick rounds or all-night sessions. You decide.</p></div><label className="arcade-sort">Popular this week <span>⌄</span></label></div>
      <div className="arcade-categories" role="tablist" aria-label="Game categories">{categories.map((item) => <button type="button" role="tab" aria-selected={category === item} className={category === item ? "selected" : ""} key={item} onClick={() => setCategory(item)}>{item}</button>)}</div>
      <div className="game-grid">{filteredGames.map((game) => <article className="game-card" key={game.id}><button className="game-cover" type="button" onClick={() => openGame(game)} style={{ backgroundImage: `linear-gradient(180deg, transparent 25%, rgba(8,10,16,.78) 100%), url(${game.image})` }} aria-label={`Open ${game.name}`}><span className={`game-genre genre-${game.tint}`}>{game.genre}</span><span className="game-cover-name">{game.name}</span></button><div className="game-card-body"><div><span className="game-player-count"><i /> {game.players}</span><div className="game-rating"><Heart size={13} fill="currentColor" /> 98% <span>community pick</span></div></div><button className="play-game-button" type="button" onClick={() => openGame(game)}>Play <span>→</span></button></div></article>)}</div>
      {activeGame && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setActiveGame(null); }}><div className={`modal-card game-modal ${activeGame.id === "tic-tac-toe" ? "tic-modal" : ""}`} role="dialog" aria-modal="true" aria-labelledby="game-modal-title"><button className="modal-close" type="button" aria-label="Close game" onClick={() => setActiveGame(null)}><X size={18} /></button><button className="modal-back-link" type="button" onClick={() => setActiveGame(null)}><ArrowLeft size={15} /> Back to arcade</button><span className="modal-icon game-modal-icon"><Gamepad2 size={22} /></span><p className="eyebrow">{activeGame.genre}</p><h2 id="game-modal-title">{activeGame.name}</h2><p className="modal-intro">{activeGame.description}</p>{activeGame.id === "tic-tac-toe" ? <><div className="ttt-status" aria-live="polite">{winner ? winner === "draw" ? "It’s a draw — nice match." : `${winner} wins the round!` : `${board.filter(Boolean).length % 2 === 0 ? "Your" : "Friend’s"} turn · ${board.filter(Boolean).length % 2 === 0 ? "X" : "O"}`}</div><div className="tic-tac-toe-board">{board.map((cell, index) => <button className={`tic-cell ${cell ? `cell-${cell.toLowerCase()}` : ""}`} type="button" key={index} onClick={() => playCell(index)} aria-label={`Cell ${index + 1}${cell ? `, ${cell}` : ", empty"}`} disabled={Boolean(cell) || Boolean(winner)}>{cell}</button>)}</div><button className="secondary-wide-button" type="button" onClick={() => setBoard(Array(9).fill(null))}><RotateCcw size={15} /> New round</button></> : <><div className="practice-card" style={{ backgroundImage: `linear-gradient(0deg, rgba(8,9,15,.65), transparent), url(${activeGame.image})` }}><span><i /> Practice session ready</span></div><button className="primary-wide-button" type="button" onClick={finishRound}>{claimed ? "Round complete · +25 coins" : "Finish practice round · +25 coins"} {!claimed && <span>→</span>}</button></>}{activeGame.id !== "tic-tac-toe" && <button className="secondary-wide-button invite-game-button" type="button" onClick={() => window.alert("Your friends can join from Messages.")}><UsersRound size={15} /> Invite a friend</button>}</div></div>}
    </section>
  );
}
