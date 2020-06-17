package pkg

func example1() {
  ghost s := set[1..10]
  assert s == set(seq[1 .. 10])
}

func example2() {
  assert 3 in set[1..10]
  assert 2 + 3 in set[1..10]
  assert !(42 in set[1..10])
}

func example3() {
  assert set[1..4] union set[4..8] == set[1..8]
}

func example4() {
  assert set[1..4] == set[int] { 1, 2, 3 }
  assert |set[1..4]| == 3
}

func example5() {
  assert set[4 .. 1] == set[int] { }
}

func example6() {
  assert set[-4 .. -1] == set[int] { -4, -3, -2 }
}

func example7() {
  assert set[1..10] intersection set[11..20] == set[int] { }
}

func example8() {
  assert set[1..10] setminus set[1..10] == set[int] { }
}
