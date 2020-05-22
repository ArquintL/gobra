package pkg

requires 0 < |xs|;
ensures n == xs[0];
func example1(ghost xs seq[int]) (ghost n int) {
  n = xs[0]
}

requires 0 < |xs| && 0 < |xs[0]|;
func example2(ghost xs seq[seq[bool]]) {
  ghost b := xs[0][0]
}

func example3() {
  assert seq[1..10][4] == 5
}

requires 0 < n
ensures seq[0..n][n - 1] == n - 1
func example4(ghost n int) {
}

func example5() {
  assert seq[int] { 1, 2, 3 }[1] == 2
}

requires 0 <= x && x < |xs|;
ensures xs[x = v][x] == v
func example6(ghost xs seq[int], ghost x int, ghost v int) {
}

requires 0 < |xs|;
func example7(ghost xs seq[bool]) {
  ghost if (xs[0]) { } else { }
}

requires 0 < |xs|;
func example8(ghost xs seq[bool]) {
  ghost for ;xs[0]; { }
}
