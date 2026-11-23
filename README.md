# Simulace těžby

Vícevláknová simulace dolu v Javě: dělníci těží suroviny, nákladní auta je vozí k převozu a převoz je
přepraví na druhý břeh. Semestrální práce z předmětu KIV/PGS.

## Průběh simulace

- **Předák** (1 vlákno) načte mapu ze vstupního souboru, najde všechny bloky surovin a rozděluje je
  dělníkům. Když je vše doručeno, ukončí simulaci.
- **Dělníci** (`cWorker` vláken) těží jednotlivé suroviny v bloku, každou nejvýše `tWorker` ms, a nakládají je
  na nákladní auto čekající v dole.
- **Nákladní auta.** Když je auto plné (`capLorry` surovin), odjede k převozu (nejvýše `tLorry` ms) a jeho místo
  v dole zaujme nové prázdné auto.
- **Převoz** čeká, dokud na něj nenajede `capFerry` aut, a poté vypluje. Po přeplutí jede každé auto do cíle
  (nejvýše `tLorry` ms).

Vlákna se synchronizují pomocí `CountDownLatch` (auto naloženo / auto může odjet) a `CyclicBarrier` (převoz je
plný).

### Vstupní soubor

Každé `X` představuje jednu surovinu. Skupina `X` bez mezery je jeden blok, bloky jsou oddělené mezerami.
Příklad je v souboru `ref_input.txt`.

```
XXXXXXXXXXXXX XXXXXXXXXXXXX XX XXXXXXXXXX XX XXXXXXXXX
X X X X XXXXXXXXXXXXXXXXXXXXXXXXXXXX X X XXXXXXXXXXXXXXXXX
```

### Výstupní soubor

Každá událost je zapsána na jeden řádek s časem, rolí, ID a popisem:

```
2024-03-16 20:52:13.653 | FOREMAN | 1 | blocks found: 42600 | total resources found: 195600
2024-03-16 20:52:13.742 | WORKER | 4 | resource mined in time [ms]: 9
2024-03-16 21:30:29.372 | FERRY | 1 | on its way, loaded after [ms]: 42344
2024-03-16 21:30:29.702 | LORRY | 193 | drove to target location in time [ms]: 330
```

Statistiky z výstupního souboru umí spočítat navazující práce [PGS_SP2](https://github.com/sarkadv/PGS_SP2).
