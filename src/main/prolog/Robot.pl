gridbound(3).
init(s(0, 0)).
goal(s(B, B)):- gridbound(B).

% move(?Dir, @Pos, ?NewPos)
move(up   , s(X, Y), s(X2, Y)):- X > 0, X2 is X - 1.
move(down , s(X, Y), s(X2, Y)):- gridbound(B), X < B, X2 is X + 1.
move(left , s(X, Y), s(X, Y2)):- Y > 0, Y2 is Y - 1.
move(right, s(X, Y), s(X, Y2)):- gridbound(B), Y < B, Y2 is Y + 1.

% plan(+MaxN, -Trace)
plan(N, T):- init(P), plan(N, T, P).
plan(_, [], P):- goal(P), !.
plan(0, [], _):- !, fail.
plan(N, [Cmd|T], P):- move(Cmd, P, Pn), Nn is N - 1, plan(Nn, T, Pn).