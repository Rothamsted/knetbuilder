-- phpMyAdmin SQL Dump
-- version 2.11.8.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 02, 2009 at 02:12 AM
-- Server version: 5.0.67
-- PHP Version: 5.2.6-2ubuntu4.2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `OndexDB`
--

-- --------------------------------------------------------

--
-- Table structure for table `attributeName`
--

CREATE TABLE IF NOT EXISTS `attributeName` (
  `sid` int(11) NOT NULL,
  `id` varchar(60) NOT NULL,
  `fullname` varchar(150) default NULL,
  `description` varchar(2000) default NULL,
  `unit` varchar(60) default NULL,
  `class` varchar(120) default NULL,
  `specOf` varchar(60) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `attributeName`
--


-- --------------------------------------------------------

--
-- Table structure for table `concept`
--

CREATE TABLE IF NOT EXISTS `concept` (
  `sid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `parser_id` varchar(120) default NULL,
  `CV` varchar(120) default NULL,
  `conceptClass` varchar(120) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `concept`
--


-- --------------------------------------------------------

--
-- Table structure for table `conceptAccession`
--

CREATE TABLE IF NOT EXISTS `conceptAccession` (
  `sid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `accession` varchar(400) default NULL,
  `ambi` tinyint(1) default NULL,
  `CV` varchar(60) default NULL,
  KEY `sid` (`sid`),
  KEY `id` (`id`),
  KEY `sid_id_acc` (`sid`,`id`,`accession`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `conceptAccession`
--


-- --------------------------------------------------------

--
-- Table structure for table `conceptClass`
--

CREATE TABLE IF NOT EXISTS `conceptClass` (
  `sid` int(11) NOT NULL,
  `id` varchar(60) NOT NULL,
  `fullname` varchar(150) default NULL,
  `description` varchar(2000) default NULL,
  `specOf` varchar(60) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `conceptClass`
--


-- --------------------------------------------------------

--
-- Table structure for table `conceptName`
--

CREATE TABLE IF NOT EXISTS `conceptName` (
  `sid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `name` varchar(150) default NULL,
  `pref` tinyint(1) default NULL,
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `conceptName`
--


-- --------------------------------------------------------

--
-- Table structure for table `concept_extras`
--

CREATE TABLE IF NOT EXISTS `concept_extras` (
  `sid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `annotation` varchar(4000) default NULL,
  `description` varchar(4000) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `concept_extras`
--


-- --------------------------------------------------------

--
-- Table structure for table `context`
--

CREATE TABLE IF NOT EXISTS `context` (
  `sid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `elementType` varchar(12) default NULL,
  `context` int(11) default NULL,
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `context`
--


-- --------------------------------------------------------

--
-- Table structure for table `cv`
--

CREATE TABLE IF NOT EXISTS `cv` (
  `sid` int(11) NOT NULL,
  `id` varchar(60) NOT NULL,
  `fullname` varchar(150) default NULL,
  `description` varchar(2000) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `cv`
--


-- --------------------------------------------------------

--
-- Table structure for table `evidence`
--

CREATE TABLE IF NOT EXISTS `evidence` (
  `sid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `elementType` varchar(12) default NULL,
  `evidence_id` varchar(120) default NULL,
  KEY `sid` (`sid`),
  KEY `id` (`id`),
  KEY `sid_id` (`sid`,`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `evidence`
--


-- --------------------------------------------------------

--
-- Table structure for table `evidenceType`
--

CREATE TABLE IF NOT EXISTS `evidenceType` (
  `sid` int(11) NOT NULL,
  `id` varchar(60) NOT NULL,
  `fullname` varchar(150) default NULL,
  `description` varchar(2000) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `evidenceType`
--


-- --------------------------------------------------------

--
-- Table structure for table `GDS_Blob`
--

CREATE TABLE IF NOT EXISTS `GDS_Blob` (
  `sid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `elementType` varchar(12) default NULL,
  `attrName` varchar(60) default NULL,
  `value` blob,
  `isDoIndex` tinyint(1) default NULL,
  `class` varchar(120) default NULL,
  KEY `sid` (`sid`),
  KEY `id` (`id`),
  KEY `sid_id_eleTy_attr` (`sid`,`id`,`attrName`,`elementType`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `GDS_Blob`
--


-- --------------------------------------------------------

--
-- Table structure for table `graph`
--

CREATE TABLE IF NOT EXISTS `graph` (
  `sid` int(11) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` tinytext NOT NULL,
  `readOnly` tinyint(1) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `graph`
--


-- --------------------------------------------------------

--
-- Table structure for table `relation`
--

CREATE TABLE IF NOT EXISTS `relation` (
  `sid` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `from` int(11) default NULL,
  `to` int(11) default NULL,
  `qual` int(11) default NULL,
  `relationType` varchar(120) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`),
  KEY `id` (`id`),
  KEY `from` (`from`),
  KEY `to` (`to`),
  KEY `qual` (`qual`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `relation`
--


-- --------------------------------------------------------

--
-- Table structure for table `relationType`
--

CREATE TABLE IF NOT EXISTS `relationType` (
  `sid` int(11) NOT NULL,
  `id` varchar(60) NOT NULL,
  `fullname` varchar(150) default NULL,
  `description` varchar(2000) default NULL,
  `inverse` varchar(150) default NULL,
  `sym` tinyint(1) default NULL,
  `antisym` tinyint(1) default NULL,
  `refl` tinyint(1) default NULL,
  `trans` tinyint(1) default NULL,
  `specOf` varchar(60) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `relationType`
--


-- --------------------------------------------------------

--
-- Table structure for table `unit`
--

CREATE TABLE IF NOT EXISTS `unit` (
  `sid` int(11) NOT NULL,
  `id` varchar(60) NOT NULL,
  `fullname` varchar(150) default NULL,
  `description` varchar(2000) default NULL,
  PRIMARY KEY  (`sid`,`id`),
  KEY `sid` (`sid`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `unit`
--

