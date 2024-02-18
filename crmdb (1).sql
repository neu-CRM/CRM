-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Gép: 127.0.0.1
-- Létrehozás ideje: 2024. Feb 05. 08:06
-- Kiszolgáló verziója: 10.4.28-MariaDB
-- PHP verzió: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Adatbázis: `crmdb`
--

-- --------------------------------------------------------

--
-- Tábla szerkezet ehhez a táblához `eladas`
--

CREATE TABLE `eladas` (
  `Vaz` int(11) NOT NULL,
  `Taz` int(11) NOT NULL,
  `Datum` date DEFAULT NULL,
  `Ar` double DEFAULT NULL,
  `EladID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;

--
-- A tábla adatainak kiíratása `eladas`
--

INSERT INTO `eladas` (`Vaz`, `Taz`, `Datum`, `Ar`, `EladID`) VALUES
(1, 1, '2024-02-01', 1500, 3),
(2, 2, '2024-02-02', 2000, 4),
(3, 3, '2024-02-03', 1800, 5),
(4, 1, '2024-02-04', 1600, 6),
(5, 2, '2024-02-05', 2100, 7),
(6, 3, '2024-02-06', 1900, 8),
(7, 1, '2024-02-07', 1550, 9),
(8, 2, '2024-02-08', 2050, 10),
(9, 3, '2024-02-09', 1750, 11),
(10, 1, '2024-02-10', 1650, 12),
(11, 2, '2024-02-11', 1980, 13),
(12, 3, '2024-02-12', 1850, 14),
(13, 1, '2024-02-13', 1520, 15),
(14, 2, '2024-02-14', 2200, 16),
(15, 3, '2024-02-15', 1950, 17),
(16, 1, '2024-02-16', 1700, 18),
(17, 2, '2024-02-17', 2020, 19),
(18, 3, '2024-02-18', 1880, 20),
(19, 1, '2024-02-19', 1620, 21),
(20, 2, '2024-02-20', 2150, 22),
(21, 3, '2024-02-21', 1970, 23),
(22, 1, '2024-02-22', 1580, 24),
(23, 2, '2024-02-23', 2080, 25),
(24, 3, '2024-02-24', 1820, 26),
(25, 1, '2024-02-25', 1680, 27),
(26, 2, '2024-02-26', 2250, 28),
(27, 3, '2024-02-27', 2000, 29),
(28, 1, '2024-02-28', 1750, 30),
(29, 2, '2024-02-29', 2100, 31),
(30, 3, '2024-03-01', 1900, 32);

-- --------------------------------------------------------

--
-- Tábla szerkezet ehhez a táblához `feladat`
--

CREATE TABLE `feladat` (
  `Vaz` int(11) NOT NULL,
  `TID` int(11) NOT NULL,
  `Leiras` text DEFAULT NULL,
  `Hatarido` date DEFAULT NULL,
  `Prioritas` int(11) DEFAULT NULL,
  `FeladatID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;

-- --------------------------------------------------------

--
-- Tábla szerkezet ehhez a táblához `kiszallitas`
--

CREATE TABLE `kiszallitas` (
  `SzalID` int(11) NOT NULL,
  `SzalDatum` date DEFAULT NULL,
  `Megjegyzes` text DEFAULT NULL,
  `Status` varchar(255) DEFAULT NULL,
  `SzallitCim` text DEFAULT NULL,
  `EladID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;

-- --------------------------------------------------------

--
-- Tábla szerkezet ehhez a táblához `partner`
--

CREATE TABLE `partner` (
  `Paz` int(11) NOT NULL,
  `Pnev` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;

-- --------------------------------------------------------

--
-- Tábla szerkezet ehhez a táblához `termek`
--

CREATE TABLE `termek` (
  `Taz` int(11) NOT NULL,
  `Tnev` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;

-- --------------------------------------------------------

--
-- Tábla szerkezet ehhez a táblához `ticket`
--

CREATE TABLE `ticket` (
  `Vaz` int(11) NOT NULL,
  `TID` int(11) NOT NULL,
  `Message` text DEFAULT NULL,
  `SubmitDate` date DEFAULT NULL,
  `ThreadID` int(11) DEFAULT NULL,
  `EladasID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;

-- --------------------------------------------------------

--
-- Tábla szerkezet ehhez a táblához `vasarlo`
--

CREATE TABLE `vasarlo` (
  `Vaz` int(11) NOT NULL,
  `Vnev` varchar(255) DEFAULT NULL,
  `Vcim` varchar(255) DEFAULT NULL,
  `Vtelszam` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_hungarian_ci;

--
-- Indexek a kiírt táblákhoz
--

--
-- A tábla indexei `eladas`
--
ALTER TABLE `eladas`
  ADD PRIMARY KEY (`EladID`),
  ADD KEY `FK_Eladas_Taz` (`Taz`),
  ADD KEY `FK_Eladas_Vaz` (`Vaz`);

--
-- A tábla indexei `feladat`
--
ALTER TABLE `feladat`
  ADD PRIMARY KEY (`FeladatID`),
  ADD KEY `FK_Feladat_Vaz` (`Vaz`),
  ADD KEY `FK_Feladat_TID` (`TID`);

--
-- A tábla indexei `kiszallitas`
--
ALTER TABLE `kiszallitas`
  ADD PRIMARY KEY (`SzalID`),
  ADD KEY `FK_KiSzallitas_EladID` (`EladID`);

--
-- A tábla indexei `partner`
--
ALTER TABLE `partner`
  ADD PRIMARY KEY (`Paz`);

--
-- A tábla indexei `termek`
--
ALTER TABLE `termek`
  ADD PRIMARY KEY (`Taz`);

--
-- A tábla indexei `ticket`
--
ALTER TABLE `ticket`
  ADD PRIMARY KEY (`TID`),
  ADD KEY `FK_Ticket_Vaz` (`Vaz`),
  ADD KEY `FK_ticket_EladasID` (`EladasID`);

--
-- A tábla indexei `vasarlo`
--
ALTER TABLE `vasarlo`
  ADD PRIMARY KEY (`Vaz`);

--
-- A kiírt táblák AUTO_INCREMENT értéke
--

--
-- AUTO_INCREMENT a táblához `eladas`
--
ALTER TABLE `eladas`
  MODIFY `EladID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT a táblához `feladat`
--
ALTER TABLE `feladat`
  MODIFY `FeladatID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT a táblához `kiszallitas`
--
ALTER TABLE `kiszallitas`
  MODIFY `SzalID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT a táblához `partner`
--
ALTER TABLE `partner`
  MODIFY `Paz` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT a táblához `termek`
--
ALTER TABLE `termek`
  MODIFY `Taz` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT a táblához `ticket`
--
ALTER TABLE `ticket`
  MODIFY `TID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT a táblához `vasarlo`
--
ALTER TABLE `vasarlo`
  MODIFY `Vaz` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
