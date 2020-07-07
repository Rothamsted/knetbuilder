-- phpMyAdmin SQL Dump
-- version 3.2.2.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 11, 2009 at 02:29 PM
-- Server version: 5.1.37
-- PHP Version: 5.2.10-2ubuntu6.1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `OndexDB2`
--

-- --------------------------------------------------------

--
-- Table structure for table `attributename`
--

CREATE TABLE IF NOT EXISTS `attributename` (
  `sid` bigint(11) NOT NULL,
  `id` varchar(200) NOT NULL,
  `fullname` varchar(500) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `unit` varchar(200) DEFAULT NULL,
  `class` varchar(200) DEFAULT NULL,
  `specOf` varchar(200) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `concept`
--

CREATE TABLE IF NOT EXISTS `concept` (
  `sid` bigint(11) NOT NULL,
  `id` int(11) NOT NULL,
  `parser_id` varchar(200) DEFAULT NULL,
  `CV` varchar(200) DEFAULT NULL,
  `conceptClass` varchar(200) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `conceptaccession`
--

CREATE TABLE IF NOT EXISTS `conceptaccession` (
  `key` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sid` bigint(11) NOT NULL,
  `id` int(11) NOT NULL,
  `accession` varchar(500) DEFAULT NULL,
  `ambi` tinyint(1) DEFAULT NULL,
  `CV` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`key`),
  KEY `id` (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10570 ;

-- --------------------------------------------------------

--
-- Table structure for table `conceptclass`
--

CREATE TABLE IF NOT EXISTS `conceptclass` (
  `sid` bigint(11) NOT NULL,
  `id` varchar(200) NOT NULL,
  `fullname` varchar(500) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `specOf` varchar(200) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `conceptname`
--

CREATE TABLE IF NOT EXISTS `conceptname` (
  `key` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `sid` bigint(11) NOT NULL,
  `id` int(11) NOT NULL,
  `name` varchar(500) DEFAULT NULL,
  `pref` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`key`),
  KEY `id` (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6226 ;

-- --------------------------------------------------------

--
-- Table structure for table `concept_extras`
--

CREATE TABLE IF NOT EXISTS `concept_extras` (
  `sid` bigint(11) NOT NULL,
  `id` int(11) NOT NULL,
  `annotation` varchar(4000) DEFAULT NULL,
  `description` varchar(4000) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `context`
--

CREATE TABLE IF NOT EXISTS `context` (
  `sid` bigint(11) NOT NULL,
  `id` int(11) NOT NULL,
  `elementType` varchar(12) DEFAULT NULL,
  `context` int(11) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `cv`
--

CREATE TABLE IF NOT EXISTS `cv` (
  `sid` bigint(11) NOT NULL,
  `id` varchar(200) NOT NULL,
  `fullname` varchar(500) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `evidence`
--

CREATE TABLE IF NOT EXISTS `evidence` (
  `sid` bigint(11) NOT NULL,
  `id` int(11) NOT NULL,
  `elementType` varchar(12) DEFAULT NULL,
  `evidence_id` varchar(200) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `evidencetype`
--

CREATE TABLE IF NOT EXISTS `evidencetype` (
  `sid` bigint(11) NOT NULL,
  `id` varchar(200) NOT NULL,
  `fullname` varchar(500) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `gds_blob`
--

CREATE TABLE IF NOT EXISTS `gds_blob` (
  `key` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sid` bigint(11) NOT NULL,
  `id` int(11) NOT NULL,
  `elementType` varchar(12) DEFAULT NULL,
  `attrName` varchar(200) DEFAULT NULL,
  `value` blob,
  `isDoIndex` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`key`),
  KEY `id` (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=20845 ;

-- --------------------------------------------------------

--
-- Table structure for table `graph`
--

CREATE TABLE IF NOT EXISTS `graph` (
  `sid` bigint(11) NOT NULL,
  `name` varchar(500) NOT NULL,
  `description` tinytext NOT NULL,
  `readOnly` tinyint(1) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `relation`
--

CREATE TABLE IF NOT EXISTS `relation` (
  `sid` bigint(11) NOT NULL,
  `id` int(11) NOT NULL,
  `fromC` int(11) DEFAULT NULL,
  `toC` int(11) DEFAULT NULL,
  `qual` int(11) DEFAULT NULL,
  `relationType` varchar(200) DEFAULT NULL,
  KEY `id` (`id`),
  KEY `idx_fromC` (`fromC`),
  KEY `idx_toC` (`toC`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `relationtype`
--

CREATE TABLE IF NOT EXISTS `relationtype` (
  `sid` bigint(11) NOT NULL,
  `id` varchar(200) NOT NULL,
  `fullname` varchar(500) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `inverse` varchar(500) DEFAULT NULL,
  `sym` tinyint(1) DEFAULT NULL,
  `antisym` tinyint(1) DEFAULT NULL,
  `refl` tinyint(1) DEFAULT NULL,
  `trans` tinyint(1) DEFAULT NULL,
  `specOf` varchar(200) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `unit`
--

CREATE TABLE IF NOT EXISTS `unit` (
  `sid` bigint(11) NOT NULL,
  `id` varchar(200) NOT NULL,
  `fullname` varchar(500) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
