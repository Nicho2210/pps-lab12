init(s(0,0)).

goal(s(3,3)).

% move(?Dir, @Pos, ?NewPos)
move(up   , s(X, Y), s(X2, Y)):- X > 0, X2 is X - 1.
move(down , s(X, Y), s(X2, Y)):- X < 3, X2 is X + 1.
move(left , s(X, Y), s(X, Y2)):- Y > 0, Y2 is Y - 1.
move(right, s(X, Y), s(X, Y2)):- Y < 3, Y2 is Y + 1.

% plan(+MaxN, -Trace)
plan(N, T):- init(P), plan(N, T, P).
plan(_, [], P):- goal(P), !.
plan(0, [], _):- fail.
plan(N, [D|T], P):- move(D, P, P1), N1 is N - 1, plan(N1, T, P1).